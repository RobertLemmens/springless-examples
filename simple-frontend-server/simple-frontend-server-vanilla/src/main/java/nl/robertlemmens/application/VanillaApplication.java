package nl.robertlemmens.application;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class VanillaApplication {

    private static final String NOT_FOUND_MESSAGE = "Resource not found";

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/", exchange -> {
                OutputStream outputStream = exchange.getResponseBody();
                InputStream index;
                String context = exchange.getRequestURI().getPath();

                if ("/".equals(context)) {
                    index = getStaticResource("index.html");
                } else {
                    index = getStaticResource(context.substring(1));
                }

                if (index == null) {
                    exchange.sendResponseHeaders(404, NOT_FOUND_MESSAGE.length());
                    outputStream.write(NOT_FOUND_MESSAGE.getBytes(StandardCharsets.UTF_8));
                } else {
                    exchange.sendResponseHeaders(200, index.available());
                    index.transferTo(outputStream);
                    outputStream.flush();
                }
                outputStream.close();
            });
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            long endTime = System.currentTimeMillis();
            System.out.println(" Server started on port 8080 in " + (endTime - startTime) + " milliseconds");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InputStream getStaticResource(String file) {
        return VanillaApplication.class.getClassLoader().getResourceAsStream(file);
    }

}
