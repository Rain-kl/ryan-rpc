package io.ryan.protocol.server;

import io.ryan.common.dto.ServiceURI;
import io.ryan.serviceCenter.ServiceCenter;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RpcServerBuilder extends RpcServer {

    private String host;
    private Integer port;
    private Class<?> rpcServer;

    private ServiceCenter serviceCenter;


    public RpcServerBuilder(String host, Integer port, Class<?> rpcServer, ServiceCenter serviceCenter) {
        super(host, port);
        this.host = host;
        this.port = port;
        this.rpcServer = rpcServer;
        this.serviceCenter = serviceCenter;
    }

    @Override
    public void start() {
        try {
            RpcServer server = (RpcServer) rpcServer.getDeclaredConstructor(String.class, Integer.class)
                    .newInstance(host, port);
            String protocol = server.getProtocol();
            serviceCenter.start(new ServiceURI(host, port, protocol));
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Failed to start RPC server", e);
        }
    }

    @Override
    public String getProtocol() {
        throw new UnsupportedOperationException();
    }
}
