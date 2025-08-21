package io.ryan.protocol.server;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcServerBuilder implements RpcServer {

    private String host;
    private Integer port;
    private Class<?> rpcServer;

    public RpcServerBuilder(String host, Integer port, Class<?> rpcServer) {
        this.host = host;
        this.port = port;
        this.rpcServer = rpcServer;
    }

    @Override
    public void start() {
        try {
            RpcServer server = (RpcServer) rpcServer.getDeclaredConstructor(String.class, Integer.class).newInstance(host, port);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start RPC server", e);
        }
    }
}
