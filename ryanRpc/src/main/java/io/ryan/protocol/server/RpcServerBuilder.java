package io.ryan.protocol.server;

import io.ryan.common.dto.ServiceURI;
import io.ryan.serviceCenter.ServiceCenter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RpcServerBuilder extends AbstractRpcServer {

    private String host;
    private Integer port;
    private Class<?> rpcServer;
    private String protocol; // 添加协议字段，用于SPI

    private ServiceCenter serviceCenter;

    public RpcServerBuilder(String host, Integer port, Class<?> rpcServer, ServiceCenter serviceCenter) {
        super(host, port);
        this.host = host;
        this.port = port;
        this.rpcServer = rpcServer;
        this.serviceCenter = serviceCenter;
    }

    public RpcServerBuilder(String host, Integer port, String protocol, ServiceCenter serviceCenter) {
        super(host, port);
        this.host = host;
        this.port = port;
        this.protocol = protocol;
        this.serviceCenter = serviceCenter;
    }

    /**
     * 创建基于协议的RpcServerBuilder
     * 使用SPI机制自动选择服务器实现
     */
    public static RpcServerBuilder forProtocol(String protocol, String host, Integer port,
            ServiceCenter serviceCenter) {
        return new RpcServerBuilder(host, port, protocol, serviceCenter);
    }

    /**
     * 创建基于指定类的RpcServerBuilder
     * 兼容原有的使用方式
     */
    public static RpcServerBuilder forClass(Class<?> rpcServerClass, String host, Integer port,
            ServiceCenter serviceCenter) {
        return new RpcServerBuilder(host, port, rpcServerClass, serviceCenter);
    }

    @Override
    public void start() {
        try {
            RpcServer server;
            String serverProtocol;

            if (protocol != null) {
                // 使用SPI方式创建服务器
                server = RpcServerFactory.createServer(protocol, host, port);
                serverProtocol = server.getProtocol();
            } else if (rpcServer != null) {
                // 兼容原有的Class方式
                server = (RpcServer) rpcServer.getDeclaredConstructor(String.class, Integer.class)
                        .newInstance(host, port);
                serverProtocol = server.getProtocol();
            } else {
                throw new IllegalArgumentException("Either protocol or rpcServer must be specified");
            }

            serviceCenter.start(new ServiceURI(host, port, serverProtocol));
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start RPC server", e);
        }
    }

    /**
     * 不需要实现此方法，实际的RpcServer实例会提供协议
     */
    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }
}
