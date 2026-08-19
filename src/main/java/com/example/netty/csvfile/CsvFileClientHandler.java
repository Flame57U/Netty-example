package com.example.netty.csvfile;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.FileRegion;

import java.io.RandomAccessFile;

public class CsvFileClientHandler extends ChannelInboundHandlerAdapter {

    private final RandomAccessFile raf;
    private final long fileSize;
    private long startTime;

    public CsvFileClientHandler(RandomAccessFile raf, long fileSize) {
        this.raf = raf;
        this.fileSize = fileSize;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        startTime = System.currentTimeMillis();

        FileRegion fileRegion = new DefaultFileRegion(raf.getChannel(), 0, fileSize);
        ChannelFuture sendFuture = ctx.writeAndFlush(fileRegion);

        sendFuture.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                long elapsed = System.currentTimeMillis() - startTime;
                double speedMBps = elapsed > 0
                        ? (fileSize / 1024.0 / 1024.0) / (elapsed / 1000.0)
                        : 0;
                System.out.println("[CLIENT] Transfer complete: " + fileSize + " bytes in "
                        + elapsed + "ms (" + String.format("%.2f", speedMBps) + " MB/s)");
            } else {
                System.err.println("[CLIENT] Transfer failed: " + future.cause().getMessage());
            }
            ctx.close();
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
