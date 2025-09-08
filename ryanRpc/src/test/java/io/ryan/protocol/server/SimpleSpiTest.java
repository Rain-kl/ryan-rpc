package io.ryan.protocol.server;

/**
 * 简单的SPI测试程序
 */
public class SimpleSpiTest {

    public static void main(String[] args) {
        System.out.println("=== 测试 RpcServerFactory SPI 机制 ===");

        try {
            // 测试获取支持的协议
            System.out.println("支持的协议: " + RpcServerFactory.getSupportedProtocols());

            // 测试创建TCP服务器
            System.out.println("\n创建TCP服务器:");
            RpcServer tcpServer = RpcServerFactory.createServer("tcp", "localhost", 8080);
            System.out.println("服务器类型: " + tcpServer.getClass().getSimpleName());
            System.out.println("协议: " + tcpServer.getProtocol());

            // 测试创建HTTP服务器
            System.out.println("\n创建HTTP服务器:");
            RpcServer httpServer = RpcServerFactory.createServer("http", "localhost", 8081);
            System.out.println("服务器类型: " + httpServer.getClass().getSimpleName());
            System.out.println("协议: " + httpServer.getProtocol());

            System.out.println("\n=== SPI机制工作正常 ===");

        } catch (Exception e) {
            System.err.println("SPI测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
