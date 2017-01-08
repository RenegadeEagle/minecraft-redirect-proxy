package co.renegadeeagle.mcproxy.codec;

import co.renegadeeagle.mcproxy.SocketState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;


public class MinecraftDecoder extends ChannelInboundHandlerAdapter {
    final static AttributeKey<SocketState> socketState = AttributeKey.valueOf("socketstate");

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        SocketState soketState = ctx.channel().attr(socketState).get();
        if (socketState == null) {
            //init socket.
            ctx.channel().attr(socketState).set(SocketState.HANDSHAKE);
            final int packetLength = readVarInt(buf);
            final int packetID = readVarInt(buf);
            System.out.printf("Received packet with length %d and id %d", packetLength, packetID);
            if (packetID == 0) {
                final int clientVersion = readVarInt(buf);
                final String hostname = readString(buf);
                final int port = buf.readUnsignedShort();
                final int state = readVarInt(buf);
                System.out.printf("received handshake packet with client version %d, hostname %s, port %d, and state %d", clientVersion, hostname, port, state);

                EventLoopGroup workerGroup = new NioEventLoopGroup();
                Bootstrap b = new Bootstrap(); // (1)
                b.group(workerGroup); // (2)
                b.channel(NioSocketChannel.class); // (3)
                b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                b.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(this);
                        ch.pipeline().addLast("encoder", new MinecraftEncoder());
                        ByteBuf sendBuf = Unpooled.buffer();
                        writeVarInt(packetLength, sendBuf);
                        writeVarInt(packetID, sendBuf);
                        writeVarInt(clientVersion, sendBuf);
                        writeString(hostname, sendBuf);
                        writeVarInt(port, sendBuf);
                        writeVarInt(state, sendBuf);
                        ch.write(sendBuf);
                    }
                });
                ChannelFuture f = b.connect(hostname, port).sync(); // (5)
                f.channel().closeFuture().sync();
            }
        } else {
            System.out.println("Whatever.");
        }
    }

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
}
