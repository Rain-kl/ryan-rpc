package io.ryan;

import io.ryan.common.constant.RpcProtocol;
import io.ryan.protocol.server.AbstractRpcServer;
import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.RateLimitRegistry;
import io.ryan.ratelimit.impl.AdvancedTokenBucketRateLimitImpl;
import io.ryan.ratelimit.impl.SimpleTokenBucketRateLimitImpl;
import io.ryan.serviceCenter.AbstractServiceCenter;
import io.ryan.serviceCenter.impl.nacosImpl.NacosCenter;
import io.ryan.serviceImpl.HelloServiceImpl;

import java.util.concurrent.ConcurrentMap;

public class Main {
    public static void main(String[] args) throws InterruptedException {


        // 注册中心注册
//        AbstractServiceCenter serviceCenter = new ZKCenter("localhost", 2181);
        AbstractServiceCenter serviceCenter = new NacosCenter("localhost", 8848);
//                new LocalServiceCenter(LocalServiceCenter.Type.Server)


        AdvancedTokenBucketRateLimitImpl tokenBucketRateLimit = new AdvancedTokenBucketRateLimitImpl(10, 10);
        tokenBucketRateLimit.setWeight(HelloServiceImpl.class, 2);

        serviceCenter.setGlobalRateLimit(new SimpleTokenBucketRateLimitImpl(1, 1));
        serviceCenter.register(HelloServiceImpl.class, true, tokenBucketRateLimit);

        // 启动RPC服务器
//        RpcServer server = new RpcServerBuilder("localhost", 8080, NettyServer.class,serviceCenter);
        AbstractRpcServer server = RpcServerBuilder.forProtocol(RpcProtocol.TCP, "localhost", 8080, serviceCenter);

        ConcurrentMap<String, RateLimit> all = RateLimitRegistry.INSTANCE.getAll();
        for (String s : all.keySet()) {
            System.out.println("接口 " + s + " 的限流器为 " + all.get(s).toString());
        }

        server.start();

    }
}