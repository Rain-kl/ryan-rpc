package io.ryan.protocol.client.NettyClientImpl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.ryan.common.Message.RpcRequest;
import io.ryan.common.Message.RpcResponse;
import io.ryan.protocol.client.RpcClient;
import io.ryan.protocol.client.RpcClientAbs;

public class NettyClient extends RpcClientAbs implements RpcClient {

    public NettyClient(String hostname, Integer port) {
        super(hostname, port);
    }

    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
        // 为每次调用创建新的 EventLoopGroup，调用完成后立即关闭
        NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());

        try {
            ChannelFuture channelFuture = bootstrap.connect(getHostname(), getPort()).sync();
            Channel channel = channelFuture.channel();
            channel.writeAndFlush(rpcRequest);

            // 等待连接关闭，这时表示已经收到响应
            channel.closeFuture().sync();

            AttributeKey<RpcResponse> key = AttributeKey.valueOf("RpcResponse");
            RpcResponse rpcResponse = channel.attr(key).get();
            System.out.println(rpcResponse);

            return rpcResponse;

        } catch (InterruptedException e) {
            System.out.println("client error");
            throw new RuntimeException(e);
        } finally {
            // 确保在方法结束时关闭 EventLoopGroup
            eventLoopGroup.shutdownGracefully();
        }
    }
}
