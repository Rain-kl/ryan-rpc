package io.ryan.protocol.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class RpcServerAbs {

    private String hostname;
    private Integer port;
}
