package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.impl.NoRateLimit;
import lombok.Setter;

@Setter
public abstract class ServiceCenter {

    protected RateLimit globalRateLimit = new NoRateLimit();

    public abstract void register(Class<?> service);

    public abstract void register(Class<?> service, Boolean retry);

    public abstract void register(Class<?> service, Boolean retry, RateLimit rateLimit);

    public abstract void start(ServiceURI uri) throws Exception;

    public abstract ServiceURI serviceDiscovery(Class<?> service);

    /**
     * 检查这个服务是否可以重试,幂等性判断
     *
     * @param interfaceName 接口名, 可以通过 RpcRequest 获取
     */
    public abstract boolean checkRetry(String interfaceName);

}
