package io.ryan;

import io.ryan.common.dto.URI;
import io.ryan.common.constant.RpcProtocol;
import io.ryan.protocol.server.NettServerImpl.NettyServer;
import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.provider.ServiceProvider;
import io.ryan.register.RedisRegister;
import io.ryan.service.HelloService;
import io.ryan.serviceImpl.HelloServiceImpl;

public class Main {
    public static void main(String[] args) {

        ServiceProvider.register(HelloServiceImpl.class);
        // 注册中心注册
        RedisRegister.register(HelloService.class.getName(), new URI("localhost", 8080, RpcProtocol.TCP));

        RpcServer server = RpcServerBuilder.builder()
                .host("localhost")
                .port(8080)
                .rpcServer(NettyServer.class)
                .build();

        server.start();

    }
}