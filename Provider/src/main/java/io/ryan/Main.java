package io.ryan;

import io.ryan.protocol.server.NettServerImpl.NettyServer;
import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.provider.ServiceProvider;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.zookeeperImpl.ZKCenter;
import io.ryan.serviceImpl.HelloServiceImpl;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        ServiceProvider.register(HelloServiceImpl.class);

        // 注册中心注册
        ZKCenter zkCenter = new ZKCenter("localhost", 2181);
        zkCenter.register(HelloService.class);

        RpcServer server = RpcServerBuilder.builder()
                .host("localhost")
                .port(8080)
                .rpcServer(NettyServer.class)
                .serviceCenter(zkCenter)
                .build();

        server.start();

    }
}