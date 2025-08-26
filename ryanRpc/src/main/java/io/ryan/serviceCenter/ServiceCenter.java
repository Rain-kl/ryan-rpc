package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;

public interface ServiceCenter {

    void register(Class<?> service);

    void register(Class<?> service, Boolean retry);

    void start(ServiceURI uri) throws Exception;

    ServiceURI serviceDiscovery(Class<?> service);

    /**
     * 检查这个服务是否可以重试,幂等性判断
     *
     * @param interfaceName 接口名, 可以通过 RpcRequest 获取
     */
    boolean checkRetry(String interfaceName);
}
