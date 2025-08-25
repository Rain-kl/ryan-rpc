package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import io.ryan.provider.ServiceProvider;

public class LocalServiceCenter implements ServiceCenter {

    static ServiceURI serviceURI = null;

    public LocalServiceCenter(Type type) {
        if(type==Type.Client && serviceURI==null){
            throw new IllegalStateException("连接参数未配置");
        }
    }

    public LocalServiceCenter(String hostname, Integer port, String protocol) {
        LocalServiceCenter.serviceURI = new ServiceURI(hostname, port, protocol);
    }

    @Override
    public void register(Class<?> service) {
        ServiceProvider.register(service);
    }

    @Override
    public void start(ServiceURI uri) throws Exception {
        LocalServiceCenter.serviceURI = uri;
        System.out.println("Local Service Center started at " + uri);
    }

    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        return serviceURI;
    }

    public enum Type {
        Server, Client
    }
}
