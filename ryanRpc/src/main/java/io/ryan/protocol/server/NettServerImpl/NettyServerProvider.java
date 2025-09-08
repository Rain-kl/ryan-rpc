package io.ryan.protocol.server.NettServerImpl;

import io.ryan.common.constant.RpcProtocol;
import io.ryan.protocol.server.RpcServer;

/**
 * NettyServer的SPI提供者
 * 用于SPI机制自动发现NettyServer实现
 */
public class NettyServerProvider implements RpcServer {

    @Override
    public void start() {
        // 这是一个提供者类，不实际启动服务
        throw new UnsupportedOperationException("This is a provider class, use NettyServer directly");
    }

    @Override
    public String getProtocol() {
        return RpcProtocol.TCP;
    }
}
