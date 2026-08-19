package com.example.netty.csvfile;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.File;
import java.io.RandomAccessFile;

public class CsvFileClient {

    private final String host;
    private final int port;
    private final String filePath;

    public CsvFileClient(String host, int port, String filePath) {
        this.host = host;
        this.port = port;
        this.filePath = filePath;
    }

    public void run() throws Exception {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.err.println("[CLIENT] File not found: " + filePath);
            System.exit(1);
        }

        long fileSize = file.length();
        System.out.println("[CLIENT] Sending: " + file.getName() + " (" + fileSize + " bytes)");

        RandomAccessFile raf = new RandomAccessFile(file, "r");

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_SNDBUF, 256 * 1024)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new CsvFileClientHandler(raf, fileSize));
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();
            Channel channel = f.channel();
            System.out.println("[CLIENT] Connected to " + host + ":" + port);

            channel.closeFuture().sync();
        } finally {
            raf.close();
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.out.println("Usage: CsvFileClient <csv-file-path> [host] [port]");
            System.exit(1);
        }
        String filePath = args[0];
        String host = args.length > 1 ? args[1] : "localhost";
        int port = args.length > 2 ? Integer.parseInt(args[2]) : 9999;
        new CsvFileClient(host, port, filePath).run();
    }
}
