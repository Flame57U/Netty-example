package com.example.netty.nio;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

public class NioServer {

    public void server(int port) throws Exception {
        final ByteBuf buf = Unpooled.unreleasableBuffer(
                Unpooled.copiedBuffer("Hi!\r\n", Charset.forName("UTF-8")));
        // 创建EventLoop
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            // 创建ServerBootstrap 实例以引导和绑定服务器
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    // 创建NioServerSocketChannel 实例进行事件处理
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    //添加一个EchoServer-Handle到子Channel的ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // 初始化channel
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter(){
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    ctx.writeAndFlush(buf.duplicate())
                                            .addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });

            //  绑定the server.
            ChannelFuture f = b.bind().sync();

            // Wait until the server socket is closed.
            f.channel().closeFuture().sync();
        } finally {
            //释放所有资源
            group.shutdownGracefully().sync();
        }
    }
}
