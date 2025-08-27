package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import io.ryan.ratelimit.RateLimit;
import lombok.AllArgsConstructor;


@AllArgsConstructor
public class BaseServiceCenter extends ServiceCenter {

    private ServiceCenter serviceCenter;

    @Override
    public void register(Class<?> service) {
        serviceCenter.register(service);
    }

    @Override
    public void register(Class<?> service, Boolean retry) {
        serviceCenter.register(service, retry);
    }

    @Override
    public void register(Class<?> service, Boolean retry, RateLimit rateLimit) {
        serviceCenter.register(service, retry, rateLimit);
    }

    @Override
    public void start(ServiceURI uri) throws Exception {
        serviceCenter.start(uri);
    }

    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        return serviceCenter.serviceDiscovery(service);
    }

    @Override
    public boolean checkRetry(String interfaceName) {
        return serviceCenter.checkRetry(interfaceName);
    }

    @Override
    public void setGlobalRateLimit(RateLimit globalRateLimit) {
        serviceCenter.setGlobalRateLimit(globalRateLimit);
    }
}
