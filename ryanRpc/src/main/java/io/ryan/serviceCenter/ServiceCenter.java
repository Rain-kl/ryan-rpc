package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import io.ryan.ratelimit.RateLimit;


public  interface ServiceCenter {

    void register(Class<?> service);

    void register(Class<?> service, Boolean retry);

    /**
     * 注册服务
     * @param service 服务实现类
     * @param retry 如果服务是幂等的, 可以开启重试
     * @param rateLimit 限流器, 覆盖全局限流器
     */
    void register(Class<?> service, Boolean retry, RateLimit rateLimit);

    void start(ServiceURI uri) throws Exception;

    ServiceURI serviceDiscovery(Class<?> service);

    /**
     * 检查这个服务是否可以重试,幂等性判断
     *
     * @param interfaceName 接口名, 可以通过 RpcRequest 获取
     */
    boolean checkRetry(String interfaceName);
}
