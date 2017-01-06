package co.renegadeeagle.mcproxy;

import co.renegadeeagle.mcproxy.packet.HandshakePacket;
import co.renegadeeagle.mcproxy.streams.MinecraftInputStream;
import co.renegadeeagle.mcproxy.streams.MinecraftOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Connection implements Runnable {

    private Socket socket;
    private MinecraftInputStream in;
    private MinecraftOutputStream out;
    private boolean active = true;
    private ConnectionState state = ConnectionState.HANDSHAKE;
    private ProxiedConnection proxiedConnection = null;
    private int clientVersion = 47;

    public Connection(Socket socket) {
        this.socket = socket;
        try {
            this.in = new MinecraftInputStream(socket.getInputStream());
            this.out = new MinecraftOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            active = false;
        }
    }
    //No real way to know how many bytes we will get from the server, so we'll assume 25000 to be safe.
    private byte[] buffer = new byte[25000];

    public void run() {
        while (active) {
            try {
                if (state != ConnectionState.HANDSHAKE) {
                    int length = in.read(buffer);
                    //Since we don't want to send out all 25k bytes the client, we just send as many bytes as we have available.
                    byte[] toSend = Arrays.copyOfRange(buffer, 0, length);
                    proxiedConnection.getOut().write(toSend);
                    continue;
                }

                int packetLength = in.readVarInt();
                int packetID = in.readVarInt();
                switch (state) {
                    case HANDSHAKE:
                        if (packetID == 0) {
                            clientVersion = in.readVarInt();
                            String hostname = in.readString();
                            int port = in.readUnsignedShort();
                            int state = in.readVarInt();

                            HandshakePacket handshakePacket = new HandshakePacket(packetLength, packetID, clientVersion, hostname, port, state);

                            if (state == 1) {
                                this.state = ConnectionState.REQUEST;
                            } else if (state == 2) {
                                this.state = ConnectionState.CONNECT;
                            }

                            proxiedConnection = new ProxiedConnection(this, handshakePacket);
                            Main.getProxiedConnections().add(proxiedConnection);
                            new Thread(proxiedConnection).start();
                        }
                        break;
                }
            } catch (IOException e) {
                active = false;
            }
        }
        try {
            this.socket.close();
        } catch (IOException e) {
        }

        if(proxiedConnection != null) Main.getProxiedConnections().remove(proxiedConnection);
        Main.getProxiedConnections().remove(this);
    }
    public void sendMOTD() {
        try {
            String json = "{\"version\":{\"name\":\"PipeProxy INC.\",\"protocol\": 5},\"players\":{\"max\":1337,\"online\":0,\"sample\":[{\"name\":\"9gigsofram\",\"id\":\"4566e69f-c907-48ee-8d71-d7ba5aa00d20\"}]},\"description\":{\"text\":\"Failed to connect to requested backend server. Please contact an administartor of the proxy.\"}}";

            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            MinecraftOutputStream o = new MinecraftOutputStream(bo);
            o.writeVarInt(0);
            o.writeString(json);

            out.writeVarInt(bo.size());
            out.write(bo.toByteArray());
        } catch (IOException e) {
            //e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public ConnectionState getState() {
        return state;
    }

    public void setState(ConnectionState state) {
        this.state = state;
    }
}
