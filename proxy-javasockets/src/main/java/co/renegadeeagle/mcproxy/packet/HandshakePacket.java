package co.renegadeeagle.mcproxy.packet;

public class HandshakePacket {
    //I probably shouldn't pass off the length and packet id in this fashion, but since I'm literally only using it in one other place, who cares.
    private int length;
    private int packetID;
    private int clientVersion;
    private String hostname;
    private int port;
    private int state;

    public HandshakePacket(int length, int packetID, int clientVersion, String hostname, int port, int state) {
        this.length = length;
        this.packetID = packetID;
        this.clientVersion = clientVersion;
        this.hostname = hostname;
        this.port = port;
        this.state = state;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getPacketID() {
        return packetID;
    }

    public void setPacketID(int packetID) {
        this.packetID = packetID;
    }

    public int getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(int clientVersion) {
        this.clientVersion = clientVersion;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(short port) {
        this.port = port;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "HandshakePacket{" +
                "length=" + length +
                ", packetID=" + packetID +
                ", clientVersion=" + clientVersion +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", state=" + state +
                '}';
    }
}
