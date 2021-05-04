package nl.robertlemmens.application;

/**
 *
 *   A very simple websocket server, using normal threads.
 *   We can create something more performant with NIO, but we wont bother for now.
 *   maybe ill create an example one day.
 *
 *   this server accepts a message, and sends it back.
 *
 */
public class VanillaApplication {

    private static WebSocketServer webSocketServer;

    public static void main(String[] args) {
        webSocketServer = WebSocketServer.create(8080);
        if (webSocketServer == null)  {
            // could not create socket, kill application
            System.out.println("Error creating server. Do we have the rights to bind address/port?");
            return;
        }

        /*
            Starts the server. It will now accept connections until we kill the application
         */
        System.out.println(" Server started on port 8080");
        webSocketServer.startListening();

    }





}
