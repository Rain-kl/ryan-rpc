package io.ryan.protocol.server;

import io.ryan.common.dto.ServiceURI;
import io.ryan.serviceCenter.ServiceCenter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RpcServerBuilder implements RpcServer {

    private String host;
    private Integer port;
    private Class<?> rpcServer;

    private ServiceCenter serviceCenter;


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
