package io.ryan;

import io.ryan.protocol.server.NettServerImpl.NettyServer;
import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.ratelimit.RateLimit;
import io.ryan.ratelimit.RateLimitRegistry;
import io.ryan.ratelimit.impl.SimpleTokenBucketRateLimitImpl;
import io.ryan.serviceCenter.AbstractServiceCenter;
import io.ryan.serviceCenter.impl.zooKeeperImpl.ZKCenter;
import io.ryan.serviceImpl.HelloServiceImpl;

import java.util.concurrent.ConcurrentMap;

public class Main {
    public static void main(String[] args) throws InterruptedException {


        // 注册中心注册
        AbstractServiceCenter serviceCenter = new ZKCenter("localhost", 2181);
//                new LocalServiceCenter(LocalServiceCenter.Type.Server)


        serviceCenter.setGlobalRateLimit(new SimpleTokenBucketRateLimitImpl(1, 1));
        serviceCenter.register(HelloServiceImpl.class, true);

        RpcServer server = RpcServerBuilder.builder()
                .host("localhost")
                .port(8080)
                .rpcServer(NettyServer.class)
                .serviceCenter(serviceCenter)
                .build();
        ConcurrentMap<String, RateLimit> all = RateLimitRegistry.INSTANCE.getAll();
        for (String s : all.keySet()) {
            System.out.println("接口 " + s + " 的限流器为 " + all.get(s).toString());
        }

        server.start();

    }
}