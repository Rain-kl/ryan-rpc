package io.ryan.proxy;


import io.ryan.circuitBreaker.CircuitBreaker;
import io.ryan.circuitBreaker.CircuitBreakerProvider;
import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.common.constant.RpcProtocol;
import io.ryan.common.dto.ServiceURI;
import io.ryan.protocol.client.GuavaRetry;
import io.ryan.protocol.client.HttpClient.HttpClientImpl;
import io.ryan.protocol.client.NettyClientImpl.NettyClient;
import io.ryan.protocol.client.RpcClient;
import io.ryan.serviceCenter.ServiceCenter;
import io.ryan.serviceCenter.impl.zooKeeperImpl.ZKCenter;

import java.lang.reflect.Proxy;

public class ProxyFactory {

    public static ServiceCenter serviceCenter;
    public static CircuitBreakerProvider circuitBreakerProvider = new CircuitBreakerProvider();

    public static void setServiceCenter(ServiceCenter serviceCenter) {
        ProxyFactory.serviceCenter = serviceCenter;
    }

    public static void setCircuitBreaker(CircuitBreaker circuitBreaker) {
        ProxyFactory.circuitBreakerProvider = new CircuitBreakerProvider(circuitBreaker);
    }

    public static <T> T getProxy(Class<T> interfaceClass) throws InterruptedException {
        if (ProxyFactory.serviceCenter == null)
            ProxyFactory.serviceCenter = new ZKCenter("localhost", 2181);
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

                    //获取熔断器
                    CircuitBreaker circuitBreaker=circuitBreakerProvider.getCircuitBreaker(method.getName());

                    //判断熔断器是否允许请求经过
                    if (!circuitBreaker.allowRequest()){
                        //这里可以针对熔断做特殊处理，返回特殊值
                        return null;
                    }

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

                    //记录response的状态，上报给熔断器
                    if (rpcResponse.getCode() ==200){
                        circuitBreaker.recordSuccess();
                    }
                    if (rpcResponse.getCode()==429){
                        circuitBreaker.recordFailure();
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
