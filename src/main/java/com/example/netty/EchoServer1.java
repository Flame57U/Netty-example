package com.example.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.InetSocketAddress;

public class EchoServer1 {
    private final int port;

    public EchoServer1(int port) {
        this.port = port;
    }

    public void run() throws InterruptedException {
//        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        // 创建EventLoop
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 创建ServerBootstrap 实例以引导和绑定服务器
            ServerBootstrap b = new ServerBootstrap();
            b.group(workerGroup)
                    // 创建NioServerSocketChannel 实例进行事件处理
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    //添加一个EchoServer-Handle到子Channel的ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            // 初始化channel
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            //  绑定the server.
            ChannelFuture f = b.bind().sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        if(args.length != 1){
            System.out.println("Usage: "+ EchoServer1.class.getSimpleName()+" <port>");
        }

        int port = Integer.parseInt(args[0]);
        System.out.println("Echo server ed. Listening on " + port);
        new EchoServer1(port).run();
    }
}
