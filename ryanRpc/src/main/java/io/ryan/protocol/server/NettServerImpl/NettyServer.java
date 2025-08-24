package io.ryan.protocol.server.NettServerImpl;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.ryan.protocol.server.RpcServer;
import io.ryan.protocol.server.RpcServerAbs;


public class NettyServer extends RpcServerAbs implements RpcServer {

    public NettyServer(String hostname, Integer port) {
        super(hostname, port);
    }

    @Override
    public void start() {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new NettyInitializer());

        ChannelFuture bind = bootstrap.bind(this.getHostname(), this.getPort());
        bind.addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("服务启动成功");
            } else {
                System.out.println("服务启动失败" + future.cause());
            }
        });

    }
}
