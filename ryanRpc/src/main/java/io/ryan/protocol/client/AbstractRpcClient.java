package io.ryan.protocol.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractRpcClient implements RpcClient{
    String hostname;
    Integer port;
}
