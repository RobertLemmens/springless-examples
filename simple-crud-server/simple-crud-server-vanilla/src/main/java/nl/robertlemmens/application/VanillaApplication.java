package nl.robertlemmens.application;


import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class VanillaApplication {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/persons", new CrudHandler());
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            long endTime = System.currentTimeMillis();
            System.out.println(" Server started on port 8080 in " + (endTime - startTime) + " milliseconds");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
