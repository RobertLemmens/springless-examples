package nl.robertlemmens.application;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class WebSocketServer {


    public static final String MAGIC_WS = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    public static final String HS_RESPONSE_HEADERS = "HTTP/1.1 101 Switching Protocols\r\n"
            + "Connection: Upgrade\r\n"
            + "Upgrade: websocket\r\n"
            + "Sec-WebSocket-Accept: ";

    private final ServerSocket serverSocket;
    private List<ClientSocketConnection> clientConnections;

    private WebSocketServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        clientConnections = new ArrayList<>();
    }

    public static WebSocketServer create(int port) {
        try {
            return new WebSocketServer(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // run forever
    public void startListening() {
        System.out.println("Accepting new connections");
        while(true) {
            Socket newClient = accept();
            System.out.println("New connection");
            if (newClient != null) {
                ClientSocketConnection clientSocketConnection = new ClientSocketConnection(newClient);
                clientSocketConnection.start();
                clientConnections.add(clientSocketConnection);
            }
        }
    }

    private Socket accept() {
        try {
            return serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
