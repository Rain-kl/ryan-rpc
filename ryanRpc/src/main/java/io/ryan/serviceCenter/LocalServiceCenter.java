package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import io.ryan.provider.ServiceProvider;
import io.ryan.ratelimit.RateLimit;

public class LocalServiceCenter extends ServiceCenter {

    static ServiceURI serviceURI = null;


    public LocalServiceCenter(Type type) {
        if (type == Type.Client && serviceURI == null) {
            throw new IllegalStateException("连接参数未配置");
        }
    }

    public LocalServiceCenter(String hostname, Integer port, String protocol) {
        LocalServiceCenter.serviceURI = new ServiceURI(hostname, port, protocol);
    }

    @Override
    public void register(Class<?> service) {
        register(service, false, globalRateLimit);
    }

    @Override
    public void register(Class<?> service, Boolean retry) {
        register(service, retry, globalRateLimit);
    }

    @Override
    public void register(Class<?> service, Boolean retry, RateLimit rateLimit) {
        ServiceProvider.register(service, globalRateLimit);
    }

    @Override
    public void start(ServiceURI uri) {
        LocalServiceCenter.serviceURI = uri;
        System.out.println("Local Service Center started at " + uri);
    }

    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        return serviceURI;
    }

    @Override
    public boolean checkRetry(String interfaceName) {
        return false;
    }

    public enum Type {
        Server, Client
    }
}
