package io.ryan.serviceCenter;

import io.ryan.common.dto.ServiceURI;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BaseServiceCenter implements ServiceCenter {

    private ServiceCenter serviceCenter;

    @Override
    public void register(Class<?> service) {
        serviceCenter.register(service);
    }

    @Override
    public void start(ServiceURI uri) throws Exception {
        serviceCenter.start(uri);
    }

    @Override
    public ServiceURI serviceDiscovery(Class<?> service) {
        return serviceCenter.serviceDiscovery(service);
    }
}
