package co.renegadeeagle.mcproxy;

public class Node {
    private String hostname;
    private String remoteHostname;
    private int remoteHostPort;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getRemoteHostname() {
        return remoteHostname;
    }

    public void setRemoteHostname(String remoteHostname) {
        this.remoteHostname = remoteHostname;
    }

    public int getRemoteHostPort() {
        return remoteHostPort;
    }

    public void setRemoteHostPort(int remoteHostPort) {
        this.remoteHostPort = remoteHostPort;
    }

    public Node(String hostname, String remoteHostname, int remoteHostPort) {
        this.hostname = hostname;
        this.remoteHostname = remoteHostname;
        this.remoteHostPort = remoteHostPort;
    }
}

