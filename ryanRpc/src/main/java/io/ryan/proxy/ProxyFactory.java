package io.ryan.proxy;


import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.common.constant.RpcProtocol;
import io.ryan.common.dto.ServiceURI;
import io.ryan.loadbalance.impl.RandomLoadBalance;
import io.ryan.protocol.client.GuavaRetry;
import io.ryan.protocol.client.HttpClient.HttpClientImpl;
import io.ryan.protocol.client.NettyClientImpl.NettyClient;
import io.ryan.protocol.client.RpcClient;
import io.ryan.serviceCenter.BaseServiceCenter;
import io.ryan.serviceCenter.ServiceCenter;
import io.ryan.serviceCenter.zookeeperImpl.ZKCenter;

import java.lang.reflect.Proxy;

public class ProxyFactory {


    public static <T> T getProxy(Class<T> interfaceClass) throws InterruptedException {
        ServiceCenter serviceCenter = new BaseServiceCenter(
                new ZKCenter("localhost", 2181, new RandomLoadBalance<>())
        );
        return ProxyFactory.getProxy(interfaceClass, serviceCenter);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getProxy(Class<T> interfaceClass, ServiceCenter serviceCenter) {
//        ServiceCenter serviceCenter = new LocalServiceCenter("localhost", 8080, RpcProtocol.TCP);
        Object proxyInstance = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest rpcRequest = RpcRequest.builder()
                            .interfaceName(interfaceClass.getName())
                            .methodName(method.getName())
                            .parameterTypes(method.getParameterTypes())
                            .parameters(args).build();
//                    // 从注册中心获取服务提供者的地址列表
                    ServiceURI serviceURI = serviceCenter.serviceDiscovery(interfaceClass);
                    RpcClient rpcClient = getRpcClient(serviceURI);

                    RpcResponse rpcResponse;
                    if (serviceCenter.checkRetry(rpcRequest.getInterfaceName())) {
                        //调用retry框架进行重试操作
                        rpcResponse = GuavaRetry.sendServiceWithRetry(rpcRequest, rpcClient);
                    } else {
                        //只调用一次
                        rpcResponse = rpcClient.sendRequest(rpcRequest);
                    }
                    return rpcResponse.getData();
                });

        return (T) proxyInstance;
    }


    private static RpcClient getRpcClient(ServiceURI selected) {
        RpcClient rpcClient;
        switch (selected.getProtocol()) {
            case (RpcProtocol.TCP) -> rpcClient = new NettyClient(selected.getHostname(), selected.getPort());
            case (RpcProtocol.HTTP) -> rpcClient = new HttpClientImpl(selected.getHostname(), selected.getPort());
            default -> throw new IllegalStateException("Unexpected value: " + selected.getProtocol());
        }
        return rpcClient;
    }


}
