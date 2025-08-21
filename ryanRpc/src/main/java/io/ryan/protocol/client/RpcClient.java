package io.ryan.protocol.client;

import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;

public interface RpcClient {

    RpcResponse sendRequest(RpcRequest rpcRequest);

}
