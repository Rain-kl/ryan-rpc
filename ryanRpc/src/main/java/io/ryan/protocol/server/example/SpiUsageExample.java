package io.ryan.protocol.server.example;

import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerFactory;

/**
 * SPI使用示例
 * 演示如何使用RpcServerFactory和自定义服务器
 */
public class SpiUsageExample {

    public static void main(String[] args) {
        System.out.println("=== RpcServer SPI机制使用示例 ===\n");

        // 1. 查看所有支持的协议
        System.out.println("1. 支持的协议:");
        System.out.println(RpcServerFactory.getSupportedProtocols());
        System.out.println();

        // 2. 使用默认的Netty服务器
        System.out.println("2. 创建Netty服务器:");
        RpcServer nettyServer = RpcServerFactory.createServer("tcp", "localhost", 8080);
        System.out.println("创建的服务器类型: " + nettyServer.getClass().getSimpleName());
        System.out.println("协议: " + nettyServer.getProtocol());
        System.out.println();

        // 3. 使用默认的HTTP服务器
        System.out.println("3. 创建HTTP服务器:");
        RpcServer httpServer = RpcServerFactory.createServer("http", "localhost", 8081);
        System.out.println("创建的服务器类型: " + httpServer.getClass().getSimpleName());
        System.out.println("协议: " + httpServer.getProtocol());
        System.out.println();

        // 4. 手动注册并使用自定义服务器
        System.out.println("4. 注册并使用自定义WebSocket服务器:");
        RpcServerFactory.registerServer("websocket", WebSocketServer.class);

        if (RpcServerFactory.isProtocolSupported("websocket")) {
            RpcServer webSocketServer = RpcServerFactory.createServer("websocket", "localhost", 8082);
            System.out.println("创建的服务器类型: " + webSocketServer.getClass().getSimpleName());
            System.out.println("协议: " + webSocketServer.getProtocol());

            // 启动WebSocket服务器（仅作演示）
            webSocketServer.start();
        }

        System.out.println();

        // 5. 更新后的支持协议列表
        System.out.println("5. 更新后支持的协议:");
        System.out.println(RpcServerFactory.getSupportedProtocols());

        System.out.println("\n=== 示例结束 ===");
    }
}
