package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;

import java.net.URI;

public interface ServiceCenter {

    void register(String serviceName, ServiceURI ServiceURI);

    URI serviceDiscovery(String serviceName);

}
