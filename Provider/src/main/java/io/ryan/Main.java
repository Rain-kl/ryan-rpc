package io.ryan;

import io.ryan.common.dto.ServiceURI;
import io.ryan.common.constant.RpcProtocol;
import io.ryan.protocol.server.NettServerImpl.NettyServer;
import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.provider.ServiceProvider;
import io.ryan.service.HelloService;
import io.ryan.serviceCenter.ZKCenter;
import io.ryan.serviceImpl.HelloServiceImpl;

public class Main {
    public static void main(String[] args) {

        ServiceProvider.register(HelloServiceImpl.class);

        // 注册中心注册
        new ZKCenter().register(
                HelloService.class.getName(),
                new ServiceURI("localhost", 8080, RpcProtocol.TCP)
        );

        RpcServer server = RpcServerBuilder.builder()
                .host("localhost")
                .port(8080)
                .rpcServer(NettyServer.class)
                .build();

        server.start();

    }
}