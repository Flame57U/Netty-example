package com.example.netty.nio;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NioClient {
    public static void main(String[] args) throws Exception {

        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        InetSocketAddress address = new InetSocketAddress(8888);

        //连接服务器
        if(!channel.connect(address)){
            while(!channel.finishConnect()){
                System.out.println("continuing...");
            }
        }
        String str = "HI";
        ByteBuffer buffer = ByteBuffer.wrap(str.getBytes());

        //发送数据
        channel.write(buffer);
        System.in.read();

        //如果连接成功，就发送数据
//        channel.close();

        //ServerSocketChannel

    }
}
