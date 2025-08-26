package io.ryan;

import io.ryan.protocol.server.NettServerImpl.NettyServer;
import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.serviceCenter.BaseServiceCenter;
import io.ryan.serviceCenter.LocalServiceCenter;
import io.ryan.serviceCenter.ServiceCenter;
import io.ryan.serviceImpl.HelloServiceImpl;

public class Main {
    public static void main(String[] args) {


        // 注册中心注册
        ServiceCenter serviceCenter = new BaseServiceCenter(
//                new ZKCenter("localhost", 2181)
                new LocalServiceCenter(LocalServiceCenter.Type.Server)
        );
//        ServiceCenter zkCenter =
        serviceCenter.register(HelloServiceImpl.class);

        RpcServer server = RpcServerBuilder.builder()
                .host("localhost")
                .port(8080)
                .rpcServer(NettyServer.class)
                .serviceCenter(serviceCenter)
                .build();

        server.start();

    }
}