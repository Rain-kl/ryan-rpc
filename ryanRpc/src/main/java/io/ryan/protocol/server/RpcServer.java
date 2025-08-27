package io.ryan.protocol.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class RpcServer {

    private String hostname;
    private Integer port;

    public abstract void start();

    public abstract String getProtocol();

}
