package io.ryan.protocol.client.NettyClientImpl;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import io.ryan.common.Message.RpcResponse;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcResponse rpcResponse) {
        AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
        channelHandlerContext.channel().attr(key).set(rpcResponse);
        // 接收到响应后关闭连接
        channelHandlerContext.channel().close();
    }
}
