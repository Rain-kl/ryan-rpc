package io.ryan.provider;

import java.util.HashMap;
import java.util.Map;

public class ServiceProvider {
    //集合中存放服务的实例
    private static final Map<String,Class<?>> interfaceProvider = new HashMap<>();


    public static void register(Class<?> service){
//        String serviceName=service.getName();
        Class<?>[] interfaceName=service.getInterfaces();

        for (Class<?> clazz:interfaceName){
            interfaceProvider.put(clazz.getName(),service);
        }

    }

    public static Class<?> getService(String interfaceName) {
        return interfaceProvider.get(interfaceName);
    }
}
