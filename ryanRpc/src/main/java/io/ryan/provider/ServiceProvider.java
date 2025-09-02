package io.ryan.provider;

import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.RateLimitRegistry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    //集合中存放服务的实例
    private static final Map<String, Class<?>> interfaceProvider = new HashMap<>();

    public static void register(Class<?> service, RateLimit rateLimit) {
//        String serviceName=service.getName();
        Arrays.stream(service.getInterfaces()).forEach(
                clazz -> {
                    interfaceProvider.put(clazz.getName(), service);
                    RateLimitRegistry.INSTANCE.put(clazz.getName(), rateLimit);
                }
        );
    }

    public static Class<?> getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
