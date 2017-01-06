package co.renegadeeagle.mcproxy;

import co.renegadeeagle.mcproxy.packet.HandshakePacket;
import co.renegadeeagle.mcproxy.streams.MinecraftInputStream;
import co.renegadeeagle.mcproxy.streams.MinecraftOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class ProxiedConnection implements Runnable {

    private Socket socket = null;
    private Connection connection;
    private HandshakePacket packet;
    private MinecraftInputStream in;
    private MinecraftOutputStream out;

    public ProxiedConnection(Connection connection, HandshakePacket packet) {
        this.connection = connection;
        this.packet = packet;

        try {
            socket = new Socket("localhost", 25565);
            this.in = new MinecraftInputStream(socket.getInputStream());
            this.out = new MinecraftOutputStream(socket.getOutputStream());

            out.writeVarInt(packet.getLength());
            out.writeVarInt(packet.getPacketID());
            out.writeVarInt(packet.getClientVersion());
            out.writeString(packet.getHostname());
            out.writeShort(packet.getPort());
            out.writeVarInt(packet.getState());

        } catch (IOException e) {
            connection.sendMOTD();
            connection.setActive(false);
        }
    }

    private byte[] buffer = new byte[25000];

    public void run() {
        while (connection.isActive()) {
            try {
                int length = in.read(buffer);
                byte[] toSend = Arrays.copyOfRange(buffer, 0, length);
                connection.getOut().write(toSend);
            } catch (IOException e) {
                connection.setActive(false);
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public HandshakePacket getPacket() {
        return packet;
    }

    public void setPacket(HandshakePacket packet) {
        this.packet = packet;
    }

    public MinecraftInputStream getIn() {
        return in;
    }

    public void setIn(MinecraftInputStream in) {
        this.in = in;
    }

    public MinecraftOutputStream getOut() {
        return out;
    }

    public void setOut(MinecraftOutputStream out) {
        this.out = out;
    }
}
