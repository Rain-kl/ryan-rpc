package io.ryan.proxy;


import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.common.URL;
import io.ryan.loadbalance.LoadBalance;
import io.ryan.loadbalance.RandomLoadBalance;
import io.ryan.protocol.client.HttpClient;
import io.ryan.register.RedisRegister;

import java.lang.reflect.Proxy;
import java.util.List;

public class ProxyFactory {

    public static <T> T getProxy(Class<T> interfaceClass) {
        Object proxyInstance = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .interfaceName(interfaceClass.getName())
                            .methodName(method.getName())
                            .parameterTypes(method.getParameterTypes())
                            .parameters(args).build();


//                    // 从注册中心获取服务提供者的地址列表
                    List<URL> urls = RedisRegister.get(interfaceClass.getName());
                    //负载均衡
                    LoadBalance<URL> loadBalance = new RandomLoadBalance();
                    URL selected = loadBalance.select(urls);
//                    URL selected = new URL("localhost", 8080); // For testing, replace with actual load balancing logic
                    HttpClient httpClient = new HttpClient(selected.getHostname(), selected.getPort());
                    RpcResponse rpcResponse = httpClient.sendRequest(rpcRequest);
                    return rpcResponse.getData();
                });

        return (T) proxyInstance;
    }
}
