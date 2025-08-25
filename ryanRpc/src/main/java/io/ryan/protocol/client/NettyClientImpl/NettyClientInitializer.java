package io.ryan.protocol.client.NettyClientImpl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.ryan.Serializer.JsonSerializer;
import io.ryan.common.utils.SimpleDecoder;
import io.ryan.common.utils.SimpleEncoder;

public class NettyClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //使用自定义的编/解码器
        pipeline.addLast(new SimpleDecoder());
        pipeline.addLast(new SimpleEncoder(new JsonSerializer()));

        pipeline.addLast(new NettyClientHandler());
    }
}
