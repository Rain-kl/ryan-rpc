package io.ryan.protocol.client.NettyClientImpl;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
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

    private static final Bootstrap bootstrap;
    private static final EventLoopGroup eventLoopGroup;
    //netty客户端初始化
    static {
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                .handler(new NettyClientInitializer());
    }
    @Override
    public RpcResponse sendRequest(RpcRequest rpcRequest) {
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
        }
    }
}
