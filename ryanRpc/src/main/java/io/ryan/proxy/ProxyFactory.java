package io.ryan.proxy;

import io.ryan.common.Invocation;
import io.ryan.common.URL;
import io.ryan.loadbalance.LoadBalance;
import io.ryan.loadbalance.RandomLoadBalance;
import io.ryan.protocol.HttpClient;
import io.ryan.register.RedisRegister;

import java.lang.reflect.Proxy;
import java.util.List;

public class ProxyFactory {

    public static <T> T getProxy(Class<T> interfaceClass) {
        Object proxyInstance = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    Invocation invocation = new Invocation(interfaceClass.getName(), method.getName(), method.getParameterTypes(), args);
                    HttpClient httpClient = new HttpClient();

//                    // 从注册中心获取服务提供者的地址列表
                    List<URL> urls = RedisRegister.get(interfaceClass.getName());
                    //负载均衡
                    LoadBalance<URL> loadBalance = new RandomLoadBalance();
                    URL selected = loadBalance.select(urls);
//                    URL selected = new URL("localhost", 8080); // For testing, replace with actual load balancing logic
                    Object result = httpClient.send(selected.getHostname(), selected.getPort(), invocation);
                    return result;
                });

        return (T) proxyInstance;
    }
}
