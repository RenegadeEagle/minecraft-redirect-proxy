package co.renegadeeagle.mcproxy.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;

public class ProxyHandler  extends ChannelDuplexHandler {
    //Represents the channel that we receive from minecraft client to us (the server).
    private Channel originalChannel = null;
    public ProxyHandler(Channel originalChannel) {
        this.originalChannel = originalChannel;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);

        originalChannel.writeAndFlush(Unpooled.buffer().writeBytes(bytes));
    }
}
