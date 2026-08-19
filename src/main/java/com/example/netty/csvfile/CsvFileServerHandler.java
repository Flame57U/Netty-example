package com.example.netty.csvfile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvFileServerHandler extends ChannelInboundHandlerAdapter {

    private FileChannel fileChannel;
    private String fileName;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String remoteAddr = ctx.channel().remoteAddress().toString()
                .replace(":", "_").replace("/", "_");
        fileName = "received_" + timestamp + "_" + remoteAddr + ".csv";
        RandomAccessFile raf = new RandomAccessFile(fileName, "rw");
        fileChannel = raf.getChannel();
        System.out.println("[SERVER] New connection from " + ctx.channel().remoteAddress()
                + " -> " + fileName);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        int readable = buf.readableBytes();
        System.out.println("[SERVER] Received chunk: " + readable + " bytes"
                + " | isDirect=" + buf.isDirect()
                + " | nioBufferCount=" + buf.nioBufferCount());

        ByteBuffer[] nioBuffers = buf.nioBuffers();
        if (nioBuffers != null && nioBuffers.length > 0) {
            long written = fileChannel.write(nioBuffers);
            System.out.println("[SERVER] FileChannel.write(ByteBuffer[" + nioBuffers.length
                    + "]) -> " + written + " bytes written to disk");
        }

        buf.release();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (fileChannel != null) {
            long fileSize = fileChannel.size();
            fileChannel.close();
            System.out.println("[SERVER] Connection closed. File saved: "
                    + fileName + " (" + fileSize + " bytes)");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        try {
            if (fileChannel != null) {
                fileChannel.close();
            }
        } catch (Exception ignored) {
        }
        ctx.close();
    }
}
