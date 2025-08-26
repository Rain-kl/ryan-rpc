package io.ryan.serviceCenter.zookeeperImpl;

import io.ryan.common.dto.ServiceURI;
import io.ryan.loadbalance.LoadBalance;
import io.ryan.loadbalance.impl.RandomLoadBalance;
import io.ryan.provider.ServiceProvider;
import io.ryan.serviceCenter.ServiceCenter;
import io.ryan.serviceCenter.cache.ServiceCache;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class ZKCenter implements ServiceCenter {
    // curator 提供的zookeeper客户端
    private CuratorFramework client;
    //zookeeper根路径节点
    private static final String ROOT_PATH = "RyanRPC";
    private LoadBalance<String> loadBalancePolicy;

    private List<String> services = new ArrayList<>();

    private String hostname;
    private Integer port;

    ServiceCache serviceCache = new ServiceCache();

    public ZKCenter(String hostname, Integer port) throws InterruptedException {
        initZookeeperConnection(hostname, port);
        this.loadBalancePolicy = new RandomLoadBalance<>();
    }

    public ZKCenter(String hostname, Integer port, LoadBalance<String> loadBalancePolicy) throws InterruptedException {
        initZookeeperConnection(hostname, port);
        this.loadBalancePolicy = loadBalancePolicy;
    }

    private void initZookeeperConnection(String hostname, Integer port) throws InterruptedException {
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
        ZKDataWatcher zkDataWatcher = new ZKDataWatcher(client, serviceCache);
        zkDataWatcher.watchToUpdate(ROOT_PATH);
    }

    /**
     * 注册服务, 仅添加到本地列表
     *
     * @param service 需要被远程调用的服务类
     */
    @Override
    public void register(Class<?> service) {
        ServiceProvider.register(service);
        for(Class<?> serviceInterface:service.getInterfaces()){
            String interfaceName = serviceInterface.getName();
            if(!services.contains(interfaceName))
                services.add(interfaceName);
        }
    }

    /**
     * 提交注册的服务到zookeeper
     *
     * @param serviceURI 服务地址
     */
    @Override
    public void start(ServiceURI serviceURI) throws Exception {
        for (String serviceName : services) {
            // 因为存入的 uri 如果携带/则无法创建节点,所以需要encode
            String path = "/" + serviceName + "/" + serviceURI.encode();
            String servicePath = "/" + serviceName;
            // 先确保服务名是持久节点
            if (client.checkExists().forPath(servicePath) == null) {
                try {
                    client.create().creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(servicePath);
                } catch (KeeperException.NodeExistsException ignore) {
                }
            }
            int retry = 2;
            while (true) {
                try {
                    client.create().withMode(CreateMode.EPHEMERAL).forPath(path);
                    return; // ok
                } catch (KeeperException.NodeExistsException e) {
                    // 通常是 session 未过期导致的“幽灵节点”，或重复启动
                    // 安全做法：检查会话是否自己创建的不可行（ACL 未设置），这里直接“幂等化”处理：认为注册已存在即可
                    if (retry-- <= 0) return;
                    Thread.sleep(200L);
                }
            }

        }
    }


    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        List<String> serviceFromCache = serviceCache.getServiceFromCache(service.getName());
        if (serviceFromCache != null && !serviceFromCache.isEmpty()) {
            log.info("cache hit");
            return ServiceURI.decode(loadBalancePolicy.select(serviceFromCache));
        }
        try {
            List<String> uris = client.getChildren().forPath("/" + service.getName());
            // 这里默认用的第一个，后面加负载均衡
            serviceCache.delete(service.getName());
            for (String uri : uris) {
                serviceCache.addServiceToCache(service.getName(), uri);
            }
            return ServiceURI.decode(loadBalancePolicy.select(uris));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("服务不存在", e);
        } catch (Exception e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }
}
