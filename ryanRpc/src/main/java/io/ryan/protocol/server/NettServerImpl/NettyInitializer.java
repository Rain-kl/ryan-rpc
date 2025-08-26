package io.ryan.protocol.server.NettServerImpl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.ryan.common.utils.Serializer.JsonSerializer;
import io.ryan.protocol.codec.SimpleDecoder;
import io.ryan.protocol.codec.SimpleEncoder;

public class NettyInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new SimpleEncoder(new JsonSerializer()));
        pipeline.addLast(new SimpleDecoder());

        pipeline.addLast(new NettyHandler());
    }
}
