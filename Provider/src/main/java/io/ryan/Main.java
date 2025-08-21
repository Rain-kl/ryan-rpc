package io.ryan;


import io.ryan.common.URL;
import io.ryan.protocol.server.HttpServerImpl.HttpServer;
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
        RedisRegister.register(HelloService.class.getName(), new URL("localhost", 8080));

        RpcServer server = RpcServerBuilder.builder()
                .host("localhost")
                .port(8080)
                .rpcServer(HttpServer.class)
                .build();

        server.start();

    }
}