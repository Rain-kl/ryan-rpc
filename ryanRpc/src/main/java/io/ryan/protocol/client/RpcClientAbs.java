package io.ryan.protocol.client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RpcClientAbs {
    String hostname;
    Integer port;
}
