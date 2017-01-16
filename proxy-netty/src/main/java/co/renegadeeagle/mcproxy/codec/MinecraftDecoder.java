package co.renegadeeagle.mcproxy.codec;

import co.renegadeeagle.mcproxy.Main;
import co.renegadeeagle.mcproxy.Node;
import co.renegadeeagle.mcproxy.SocketState;
import co.renegadeeagle.mcproxy.util.PacketUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.AttributeKey;

public class MinecraftDecoder extends ChannelInboundHandlerAdapter {
    final static AttributeKey<SocketState> GLOBAL = AttributeKey.valueOf("socketstate");
    final static AttributeKey<Channel> PROXY_CHANNEL = AttributeKey.valueOf("proxychannel");

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) throws Exception {
        final ByteBuf buf = (ByteBuf) msg;
        SocketState socketState = ctx.channel().attr(GLOBAL).get();
        if (socketState == null) {
            ctx.channel().attr(GLOBAL).set(SocketState.HANDSHAKE);
            final int packetLength = readVarInt(buf);
            final int packetID = readVarInt(buf);
            if (packetID == 0) {
                final int clientVersion = readVarInt(buf);
                final String hostname = readString(buf);
                final int port = buf.readUnsignedShort();
                final int state = readVarInt(buf);

                Bootstrap b = new Bootstrap();
                b.group(Main.getWorkerGroup());
                b.channel(NioSocketChannel.class);
                b.option(ChannelOption.SO_KEEPALIVE, true);
                b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);
                b.handler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel ch) throws Exception {
                        ch.pipeline().addLast(new ProxyHandler(ctx.channel()));
                    }
                });
                Node node = nodeFromHostname(hostname);
                final ChannelFuture cf = b.connect(node.getRemoteHostname(), node.getRemoteHostPort());

                cf.addListener(new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            ByteBuf sendBuf = Unpooled.buffer();
                            writeVarInt(packetLength, sendBuf);
                            writeVarInt(packetID, sendBuf);
                            writeVarInt(clientVersion, sendBuf);
                            writeString(hostname, sendBuf);
                            writeVarShort(sendBuf, port);
                            writeVarInt(state, sendBuf);

                            while (buf.readableBytes() > 0) {
                                byte b = buf.readByte();
                                sendBuf.writeByte(b);
                            }

                            future.channel().writeAndFlush(sendBuf); //Send out the handshake + anything else we've gotten (Request or login start packet)
                            ctx.channel().attr(GLOBAL).set(SocketState.PROXY);
                            ctx.channel().attr(PROXY_CHANNEL).set(cf.channel());
                        } else {
                            ByteBuf body = PacketUtil.createStatusPacket(clientVersion);

                            ByteBuf header = Unpooled.buffer();
                            writeVarInt(body.readableBytes(), header);
                            ctx.channel().writeAndFlush(header);
                            ctx.channel().writeAndFlush(body);
                            ctx.close();
                            cf.channel().close();
                        }
                    }
                });
            }
        } else {
            Channel proxiedChannel = ctx.channel().attr(PROXY_CHANNEL).get();
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            proxiedChannel.writeAndFlush(Unpooled.buffer().writeBytes(bytes));
        }
    }

    public Node nodeFromHostname(String req) {
        for(Node node : Main.getSettings().getNodes()) {
            if(node.getHostname().equalsIgnoreCase(req)) {
                return node;
            }
        }
        return null;
    }


    /*
        All of these varint implementations comes from SpigotMC's bungeecord.
        Source: https://github.com/SpigotMC/BungeeCord/blob/master/protocol/src/main/java/net/md_5/bungee/protocol/DefinedPacket.java
     */
    public static int readVarInt(ByteBuf input) {
        return readVarInt(input, 5);
    }

    public static int readVarInt(ByteBuf input, int maxBytes) {
        int out = 0;
        int bytes = 0;
        byte in;
        while (true) {
            in = input.readByte();

            out |= (in & 0x7F) << (bytes++ * 7);

            if (bytes > maxBytes) {
                throw new RuntimeException("VarInt too big");
            }

            if ((in & 0x80) != 0x80) {
                break;
            }
        }

        return out;
    }

    public static String readString(ByteBuf buf) {
        int len = readVarInt(buf);
        if (len > Short.MAX_VALUE) {
        }

        byte[] b = new byte[len];
        buf.readBytes(b);

        return new String(b);
    }

    public static void writeVarInt(int value, ByteBuf output) {
        int part;
        while (true) {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }

            output.writeByte(part);

            if (value == 0) {
                break;
            }
        }
    }

    public static void writeString(String s, ByteBuf buf) {
        if (s.length() > Short.MAX_VALUE) {
        }

        byte[] b = s.getBytes();
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
    }

    public static int readVarShort(ByteBuf buf) {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ((high & 0xFF) << 15) | low;
    }

    public static void writeVarShort(ByteBuf buf, int toWrite) {
        int low = toWrite & 0x7FFF;
        int high = (toWrite & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        buf.writeShort(low);
        if (high != 0) {
            buf.writeByte(high);
        }
    }
}
