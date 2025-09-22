package io.ryan.serviceCenter.impl.nacosImpl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.ryan.common.dto.ServiceURI;
import io.ryan.loadbalance.LoadBalance;
import io.ryan.loadbalance.impl.RandomLoadBalance;
import io.ryan.provider.ServiceProvider;
import io.ryan.ratelimit.RateLimit;
import io.ryan.serviceCenter.AbstractServiceCenter;
import io.ryan.serviceCenter.cache.ServiceCache;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

@Slf4j
@Getter
@Setter
public class NacosCenter extends AbstractServiceCenter {

    // 服务命名空间
//    private static final String NAMESPACE = "ryan-rpc";
    private static final String NAMESPACE = "public";
    // 服务分组
    private static final String GROUP_NAME = "RyanRPC";
    // 重试白名单标识
    private static final String RETRY_METADATA_KEY = "canRetry";
    // 本地服务缓存
    ServiceCache serviceCache = new ServiceCache();
    // Nacos 命名服务客户端
    private NamingService namingService;
    // 负载均衡策略
    private LoadBalance<Instance> loadBalancePolicy;
    // 本地注册的服务列表
    private List<String> services = new ArrayList<>();
    // 服务地址和端口
    private String hostname;
    private Integer port;
    // 重试白名单
    private List<String> retryWhiteList = new ArrayList<>();

    public NacosCenter(String hostname, Integer port) throws InterruptedException {
        initNacosConnection(hostname, port);
        this.loadBalancePolicy = new RandomLoadBalance<>();
    }

    public NacosCenter(String hostname, Integer port, LoadBalance<Instance> loadBalancePolicy)
            throws InterruptedException {
        initNacosConnection(hostname, port);
        this.loadBalancePolicy = loadBalancePolicy;
    }

    /**
     * 初始化 Nacos 连接
     */
    private void initNacosConnection(String hostname, Integer port) {
        String serverAddr = hostname + ":" + port;
        Properties properties = new Properties();
        properties.setProperty("serverAddr", serverAddr);
        properties.setProperty("namespace", NAMESPACE);

        try {
            this.namingService = NamingFactory.createNamingService(properties);
        } catch (NacosException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        log.info("Connected to Nacos server: {}", serverAddr);

        // 创建数据监听器
        NacosDataWatcher nacosDataWatcher = new NacosDataWatcher(namingService, serviceCache);
        // 这里可以添加服务监听逻辑
    }

    /**
     * 注册服务，仅添加到本地列表
     */
    @Override
    public void register(Class<?> service) {
        register(service, false);
    }

    @Override
    public void register(Class<?> service, Boolean retry) {
        register(service, retry, globalRateLimit);
    }

    @Override
    public void register(Class<?> service, Boolean retry, RateLimit rateLimit) {
        ServiceProvider.register(service, rateLimit);
        for (Class<?> serviceInterface : service.getInterfaces()) {
            String interfaceName = serviceInterface.getName();
            if (!services.contains(interfaceName))
                services.add(interfaceName);
            if (retry && !retryWhiteList.contains(interfaceName))
                retryWhiteList.add(interfaceName);
        }
    }

    /**
     * 服务发现
     */
    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        List<String> serviceFromCache = serviceCache.getServiceFromCache(service.getName());
        if (serviceFromCache != null && !serviceFromCache.isEmpty()) {
//            log.info("cache hit");
            Instance selectedInstance = loadBalancePolicy.select(convertToInstances(serviceFromCache));
            return new ServiceURI(selectedInstance.getIp(), selectedInstance.getPort(), 
                    selectedInstance.getMetadata().getOrDefault("protocol", "tcp"));
        }

        try {
            List<Instance> instances = namingService.getAllInstances(service.getName(), GROUP_NAME);
            if (instances.isEmpty()) {
                throw new RuntimeException("服务不存在: " + service.getName());
            }

            // 更新缓存
            serviceCache.delete(service.getName());
            for (Instance instance : instances) {
                String instanceStr = instance.getIp() + ":" + instance.getPort();
                serviceCache.addServiceToCache(service.getName(), instanceStr);
            }

            // 使用负载均衡选择实例
            Instance selectedInstance = loadBalancePolicy.select(instances);
            return new ServiceURI(selectedInstance.getIp(), selectedInstance.getPort(),
                    selectedInstance.getMetadata().get("protocol"));

        } catch (NacosException e) {
            throw new RuntimeException("服务发现失败", e);
        }
    }

    /**
     * 提交注册的服务到 Nacos
     */
    @Override
    public void start(ServiceURI serviceURI) throws Exception {
        this.hostname = serviceURI.getHostname();
        this.port = serviceURI.getPort();

        for (String serviceName : services) {
            try {
                Instance instance = new Instance();
                instance.setIp(hostname);
                instance.setPort(port);
                instance.setMetadata(new HashMap<>() {
                    {
                        put("protocol", serviceURI.getProtocol());
                    }
                });
                instance.setHealthy(true);
                instance.setEnabled(true);
                instance.setWeight(1.0);

                // 如果服务支持重试，添加元数据
                if (retryWhiteList.contains(serviceName)) {
                    instance.getMetadata().put(RETRY_METADATA_KEY, "true");
                }

                // 注册服务实例
                namingService.registerInstance(serviceName, GROUP_NAME, instance);
                log.info("Successfully registered service: {} at {}:{}", serviceName, hostname, port);

            } catch (NacosException e) {
                log.error("Failed to register service: {}", serviceName, e);
                throw new RuntimeException("服务注册失败: " + serviceName, e);
            }
        }
    }

    /**
     * 检查服务是否可以重试
     */
    @Override
    public boolean checkRetry(String interfaceName) {
        try {
            List<Instance> instances = namingService.getAllInstances(interfaceName, GROUP_NAME);
            for (Instance instance : instances) {
                String canRetry = instance.getMetadata().get(RETRY_METADATA_KEY);
                if ("true".equals(canRetry)) {
//                    log.info("服务 {} 在重试白名单上，可进行重试", interfaceName);
                    return true;
                }
            }
        } catch (NacosException e) {
            log.error("检查重试失败", e);
        }
        return false;
    }

    /**
     * 将字符串列表转换为 Instance 列表（用于缓存数据的转换）
     */
    private List<Instance> convertToInstances(List<String> addressList) {
        List<Instance> instances = new ArrayList<>();
        for (String address : addressList) {
            String[] parts = address.split(":");
            if (parts.length == 2) {
                Instance instance = new Instance();
                instance.setIp(parts[0]);
                instance.setPort(Integer.parseInt(parts[1]));
                instances.add(instance);
            }
        }
        return instances;
    }

    /**
     * 关闭 Nacos 连接
     */
    public void shutdown() {
        try {
            if (namingService != null) {
                // 注销所有服务实例
                for (String serviceName : services) {
                    namingService.deregisterInstance(serviceName, GROUP_NAME, hostname, port);
                }
                namingService.shutDown();
                log.info("Nacos naming service shutdown successfully");
            }
        } catch (NacosException e) {
            log.error("Failed to shutdown Nacos naming service", e);
        }
    }
}
