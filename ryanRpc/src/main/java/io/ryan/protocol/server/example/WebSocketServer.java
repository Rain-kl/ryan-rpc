package io.ryan.protocol.server.example;

import io.ryan.protocol.server.AbstractRpcServer;

/**
 * 自定义WebSocket服务器实现示例
 * 演示如何创建自己的RpcServer实现
 */
public class WebSocketServer extends AbstractRpcServer {

    public WebSocketServer(String hostname, Integer port) {
        super(hostname, port);
    }

    @Override
    public void start() {
        System.out.println("启动WebSocket服务器在 " + getHostname() + ":" + getPort());
        // 这里可以添加WebSocket服务器的启动逻辑
        // 例如使用Jetty WebSocket或其他WebSocket实现

        // 模拟启动过程
        try {
            System.out.println("WebSocket服务器启动中...");
            Thread.sleep(1000); // 模拟启动时间
            System.out.println("WebSocket服务器启动成功！");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("WebSocket服务器启动失败", e);
        }
    }

    @Override
    public String getProtocol() {
        return "websocket";
    }
}
