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

        channel.register(selector, SelectionKey.OP_ACCEPT);
        //循环等待客户端链接

        int count = selector.select(2000);
        // 等待事件
        if (count > 0) {
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

                    ServerSocketChannel serverChannel =
                            (ServerSocketChannel) key.channel();

                    SocketChannel sc =
                            serverChannel.accept();

                    if (sc != null) {

                        System.out.println(
                                sc.getRemoteAddress()
                                        + " 上线"
                        );
                        // 非阻塞
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

                    SocketChannel sc =
                            (SocketChannel) key.channel();
                    ByteBuffer buffer =
                            ByteBuffer.allocate(1024);


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
