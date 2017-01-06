package co.renegadeeagle.mcproxy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Main {

    private static List<Connection> connections = new ArrayList<Connection>();
    private static List<ProxiedConnection> proxiedConnections = new ArrayList<ProxiedConnection>();

    public static void main(String[] args) throws Exception {
        System.out.println("Starting server...");

        ServerSocket serverSocket = new ServerSocket(22000);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            Connection connection = new Connection(clientSocket);

            System.out.println("Accepted connection from " + clientSocket.getRemoteSocketAddress().toString());
            connections.add(connection);
            new Thread(connection).start();
        }
    }

    public static List<Connection> getConnections() {
        return connections;
    }

    public static void setConnections(List<Connection> connections) {
        Main.connections = connections;
    }

    public static List<ProxiedConnection> getProxiedConnections() {
        return proxiedConnections;
    }

    public static void setProxiedConnections(List<ProxiedConnection> proxiedConnections) {
        Main.proxiedConnections = proxiedConnections;
    }
}
