package com.example.netty.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Server {

    public static void main(String[] args) throws IOException {
        //创建 ServerSocketChannel -> ServerSocket
        ServerSocketChannel channel = ServerSocketChannel.open();

        //得到selector 对象
        Selector selector = Selector.open();

//绑定一些端口
        channel.socket().bind(new InetSocketAddress(8888));

        //设置为非阻塞
        channel.configureBlocking(false);
//将socketchannel注册到selector，关注事件为OP_READ,
        //同时给socketchannel关联一个Buffer todo ByteBuffer.allocate(512);
        channel.register(selector, SelectionKey.OP_ACCEPT);
        //循环等待客户端链接

        // 等待事件
        while (true) {

            int count = selector.select(2000);

            if(count == 0){
                System.out.println("服务ing...");
                continue;
            }
            // 获取发生事件的key集合
            Set<SelectionKey> keys =
                    selector.selectedKeys();

            Iterator<SelectionKey> iterator =
                    keys.iterator();

            while (iterator.hasNext()) {

                SelectionKey key =
                        iterator.next();

                // 必须删除
                iterator.remove();

                // ======================
                // 新连接事件
                // ======================
                if (key.isAcceptable()) {
//                    ServerSocketChannel serverChannel =
//                            (ServerSocketChannel) key.channel();

                    SocketChannel sc =
                            channel.accept();

                    if (sc != null) {

                        System.out.println(
                                sc.getRemoteAddress()
                                        + " 上线 " + sc.hashCode()
                        );
                        // 将SocketChannel设置为非阻塞
                        sc.configureBlocking(false);

                        // 注册读事件
                        sc.register(
                                selector,
                                SelectionKey.OP_READ
                        );
                    }
                }

                // ======================
                // 读事件
                // ======================
                if (key.isReadable()) {
//          通道数据转ByteBuffer          ByteBuffer buffer = (ByteBuffer)key.attachment();

                    SocketChannel sc =
                            (SocketChannel) key.channel();
                    ByteBuffer buffer =
                            (ByteBuffer) key.attachment();

                    try {

                        int len =
                                sc.read(buffer);

                        if (len > 0) {
                            String msg =
                                    new String(
                                            buffer.array(),
                                            0,
                                            len
                                    );
                            System.out.println(
                                    "收到消息:"
                                            + msg
                            );


                            // 回复客户端
                            buffer.clear();
                            buffer.put(
                                    ("服务器收到:" + msg)
                                            .getBytes()
                            );

                            buffer.flip();
                            sc.write(buffer);
                        } else if (len == -1) {


                            System.out.println(
                                    "客户端断开"
                            );


                            key.cancel();
                            sc.close();

                        }
                    } catch (Exception e) {
                        key.cancel();

                        sc.close();

                    }
                }

            }
        }
    }
}
