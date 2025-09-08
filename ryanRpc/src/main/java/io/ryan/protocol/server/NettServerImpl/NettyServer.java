package io.ryan.protocol.server.NettServerImpl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.ryan.common.constant.RpcProtocol;
import io.ryan.protocol.server.AbstractRpcServer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NettyServer extends AbstractRpcServer {

    public static final String PROTOCOL = RpcProtocol.TCP;


    ChannelFuture channelFuture;

    public NettyServer(String hostname, Integer port) {
        super(hostname, port);
    }

    @Override
    public String getProtocol() {
        return RpcProtocol.TCP;
    }

    @Override
    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyInitializer());

        channelFuture = bootstrap.bind(this.getHostname(), this.getPort());
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("服务启动成功");
            } else {
                System.out.println("服务启动失败" + future.cause());
            }
        });

    }

}
