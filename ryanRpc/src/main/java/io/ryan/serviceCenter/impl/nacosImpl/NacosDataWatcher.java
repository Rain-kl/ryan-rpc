package io.ryan.serviceCenter.impl.nacosImpl;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import io.ryan.serviceCenter.cache.ServiceCache;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class NacosDataWatcher {

    private final NamingService namingService;
    private final ServiceCache cache;

    public NacosDataWatcher(NamingService namingService, ServiceCache cache) {
        this.namingService = namingService;
        this.cache = cache;
    }

    /**
     * 监听指定服务的实例变化
     */
    public void watchServiceInstances(String serviceName, String groupName) {
        try {
            namingService.subscribe(serviceName, groupName, new EventListener() {
                @Override
                public void onEvent(com.alibaba.nacos.api.naming.listener.Event event) {
                    if (event instanceof NamingEvent namingEvent) {
                        List<Instance> instances = namingEvent.getInstances();

                        log.info("Service {} instances changed, new count: {}", serviceName, instances.size());

                        // 清空旧的缓存
                        cache.delete(serviceName);

                        // 添加新的实例到缓存
                        for (Instance instance : instances) {
                            if (instance.isHealthy() && instance.isEnabled()) {
                                String address = instance.getIp() + ":" + instance.getPort();
                                cache.addServiceToCache(serviceName, address);
                                log.debug("Added healthy instance to cache: {} -> {}", serviceName, address);
                            }
                        }
                    }
                }
            });

            log.info("Started watching service instances for: {}", serviceName);

        } catch (Exception e) {
            log.error("Failed to watch service instances for: {}", serviceName, e);
        }
    }

    /**
     * 取消监听指定服务
     */
    public void unsubscribeService(String serviceName, String groupName) {
        try {
            namingService.unsubscribe(serviceName, groupName, new EventListener() {
                @Override
                public void onEvent(com.alibaba.nacos.api.naming.listener.Event event) {
                    // 空实现，用于取消订阅
                }
            });
            log.info("Unsubscribed from service: {}", serviceName);
        } catch (Exception e) {
            log.error("Failed to unsubscribe from service: {}", serviceName, e);
        }
    }
}
