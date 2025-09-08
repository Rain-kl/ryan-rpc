package io.ryan.protocol.server.example;

import io.ryan.protocol.server.RpcServerBuilder;
import io.ryan.serviceCenter.ServiceCenter;
import io.ryan.serviceCenter.impl.LocalServiceCenter;

/**
 * RpcServerBuilder 使用示例
 * 展示如何使用新的SPI机制
 */
public class RpcServerBuilderExample {

    public static void main(String[] args) {
        System.out.println("=== RpcServerBuilder SPI 使用示例 ===\n");

        // 创建一个模拟的ServiceCenter
        ServiceCenter serviceCenter = new LocalServiceCenter("localhost", 8080, "tcp");

        try {
            // 1. 使用协议名称创建服务器（推荐方式）
            System.out.println("1. 使用协议名称创建TCP服务器:");
            RpcServerBuilder tcpBuilder = RpcServerBuilder.forProtocol("tcp", "localhost", 8080, serviceCenter);
            System.out.println("协议: " + tcpBuilder.getProtocol());
            System.out.println("主机: " + tcpBuilder.getHost());
            System.out.println("端口: " + tcpBuilder.getPort());
            // tcpBuilder.start(); // 实际使用时调用start()启动服务器

            System.out.println("\n2. 使用协议名称创建HTTP服务器:");
            RpcServerBuilder httpBuilder = RpcServerBuilder.forProtocol("http", "localhost", 8081, serviceCenter);
            System.out.println("协议: " + httpBuilder.getProtocol());
            System.out.println("主机: " + httpBuilder.getHost());
            System.out.println("端口: " + httpBuilder.getPort());
            // httpBuilder.start(); // 实际使用时调用start()启动服务器

            // 3. 使用传统的Class方式（向后兼容）
            System.out.println("\n3. 使用Class方式创建服务器（向后兼容）:");
            Class<?> nettyServerClass = Class.forName("io.ryan.protocol.server.NettServerImpl.NettyServer");
            RpcServerBuilder classBuilder = RpcServerBuilder.forClass(nettyServerClass, "localhost", 8082,
                    serviceCenter);
            System.out.println("协议: " + classBuilder.getProtocol());
            System.out.println("主机: " + classBuilder.getHost());
            System.out.println("端口: " + classBuilder.getPort());

            System.out.println("\n=== 示例完成 ===");

        } catch (Exception e) {
            System.err.println("示例执行失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
