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

import java.net.URI;
import java.util.List;

@Data
public class ZKCenter implements ServiceCenter {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "RyanRPC";
    private LoadBalance<String> loadBalancePolicy;

    void init() {
        // 指数时间重试
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        // sessionTimeoutMs 与 zoo.cfg中的tickTime 有关系，
        // zk还会根据minSessionTimeout与maxSessionTimeout两个参数重新调整最后的超时值。默认分别为tickTime 的2倍和20倍
        // 使用心跳监听状态
        this.client = CuratorFrameworkFactory.builder()
                .connectString("127.0.0.1:2181").sessionTimeoutMs(40000).retryPolicy(policy)
                .namespace(ROOT_PATH)
                .build();
        this.client.start();
        System.out.println("zookeeper 连接成功");
    }

    public ZKCenter() {
        init();
        this.loadBalancePolicy = new RandomLoadBalance<>();
    }

    public ZKCenter(LoadBalance<String> loadBalancePolicy) {
        init();
        this.loadBalancePolicy = loadBalancePolicy;
    }


    @Override
    public void register(String serviceName, ServiceURI ServiceURI) {
        try {
            // serviceName创建成永久节点，服务提供者下线时，不删服务名，只删地址
            if (client.checkExists().forPath("/" + serviceName) == null) {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath("/" + serviceName);
            }
            // 路径地址，一个/代表一个节点
            String path = "/" + serviceName + "/" + ServiceURI.encode();
            // 临时节点，服务器下线就删除节点
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path);
        } catch (Exception e) {
            throw new RuntimeException("服务注册失败", e);
        }
    }

    @Override
    public URI serviceDiscovery(String serviceName) {
        try {
            List<String> uris = client.getChildren().forPath("/" + serviceName);
            // 这里默认用的第一个，后面加负载均衡
            return URI.create(ServiceURI.decode(loadBalancePolicy.select(uris)));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("服务不存在", e);
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }
}
