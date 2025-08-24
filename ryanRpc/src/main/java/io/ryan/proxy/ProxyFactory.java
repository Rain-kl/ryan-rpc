package io.ryan.proxy;


import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.common.dto.URI;
import io.ryan.common.constant.RpcProtocol;
import io.ryan.loadbalance.LoadBalance;
import io.ryan.loadbalance.RandomLoadBalance;
import io.ryan.protocol.client.HttpClient.HttpClientImpl;
import io.ryan.protocol.client.NettyClientImpl.NettyClient;
import io.ryan.protocol.client.RpcClient;
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
                    List<URI> URIS = RedisRegister.get(interfaceClass.getName());
                    //负载均衡
                    LoadBalance<URI> loadBalance = new RandomLoadBalance();
                    URI selected = loadBalance.select(URIS);
//                    URL selected = new URL("localhost", 8080); // For testing, replace with actual load balancing logic

                    RpcClient rpcClient = getRpcClient(selected);
                    RpcResponse rpcResponse = rpcClient.sendRequest(rpcRequest);

                    return rpcResponse.getData();
                });

        return (T) proxyInstance;
    }

    private static RpcClient getRpcClient(URI selected) {
        RpcClient rpcClient;
        switch (selected.getProtocol()) {
            case (RpcProtocol.TCP) -> {
                rpcClient = new NettyClient(selected.getHostname(), selected.getPort());
            }
            case (RpcProtocol.HTTP) -> {
                rpcClient = new HttpClientImpl(selected.getHostname(), selected.getPort());
            }
            default -> throw new IllegalStateException("Unexpected value: " + selected.getProtocol());
        }
        return rpcClient;
    }


}
