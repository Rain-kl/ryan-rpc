package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;

public interface ServiceCenter {

    void register(Class<?> service);

    void start(ServiceURI uri) throws Exception;

    ServiceURI serviceDiscovery(Class<?> service);

}
