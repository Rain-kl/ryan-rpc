package io.ryan.protocol.server.NettServerImpl;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.ryan.Serializer.JsonSerializer;
import io.ryan.common.utils.SimpleDecoder;
import io.ryan.common.utils.SimpleEncoder;

public class NettyInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new SimpleEncoder(new JsonSerializer()));
        pipeline.addLast(new SimpleDecoder());

        pipeline.addLast(new NettyHandler());
    }
}
