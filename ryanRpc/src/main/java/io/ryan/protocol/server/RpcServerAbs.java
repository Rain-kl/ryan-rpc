package io.ryan.protocol.server;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class RpcServerAbs {

    public String hostname;
    public Integer port;
}
