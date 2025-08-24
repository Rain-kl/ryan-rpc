package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import io.ryan.loadbalance.LoadBalance;
import io.ryan.loadbalance.RandomLoadBalance;
import lombok.Data;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;

@Data
public class ZKCenter implements ServiceCenter {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "RyanRPC";
    private LoadBalance<String> loadBalancePolicy;

    private List<String> services = new ArrayList<>();

    private String hostname;
    private Integer port;

    public ZKCenter(String hostname, Integer port) {
        init(hostname, port);
        this.loadBalancePolicy = new RandomLoadBalance<>();
    }

    public ZKCenter(String hostname, Integer port, LoadBalance<String> loadBalancePolicy) {
        init(hostname, port);
        this.loadBalancePolicy = loadBalancePolicy;
    }

    void init(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder()
                .connectString(hostname + ":" + port).sessionTimeoutMs(40000).retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }

    @Override
    public void register(Class<?> service) {
        services.add(service.getName());
    }

    @Override
    public void start(ServiceURI serviceURI) throws Exception {
        for (String serviceName : services) {
            // 路径地址，一个/代表一个节点
            String path = "/" + serviceName + "/" + serviceURI.encode();
            try {
                // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
                if (client.checkExists().forPath("/" + serviceName) == null)
                    client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
                // 临时节点，服务器下线就删除节点
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
            } catch (KeeperException.NodeExistsException e) {
                //删除掉重复注册的节点
                client.delete().forPath(path);
                throw new RuntimeException("服务已存在，已删除重复节点，请重新注册", e);
            }catch (Exception e) {
                throw new RuntimeException("服务注册失败", e);
            }
        }
    }


    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        try {
            List<String> uris = client.getChildren().forPath("/" + service.getName());
            // 这里默认用的第一个，后面加负载均衡
            return ServiceURI.decode(loadBalancePolicy.select(uris));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("服务不存在", e);
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }
}
