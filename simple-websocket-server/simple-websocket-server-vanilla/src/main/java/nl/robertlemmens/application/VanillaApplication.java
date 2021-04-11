package nl.robertlemmens.application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VanillaApplication {

    private static final String MAGIC_WS = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private static final String HS_RESPONSE_HEADERS = "HTTP/1.1 101 Switching Protocols\r\n"
            + "Connection: Upgrade\r\n"
            + "Upgrade: websocket\r\n"
            + "Sec-WebSocket-Accept: ";

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println(" Server started on port 8080");
        Socket client = serverSocket.accept();
        System.out.println("Client connected");

        // read / write
        InputStream in = client.getInputStream();
        OutputStream out = client.getOutputStream();
        Scanner s = new Scanner(in, StandardCharsets.UTF_8);

        try {
            handshake(s, out);
            decode(new byte[] { (byte) 198, (byte) 131, (byte) 130, (byte) 182, (byte) 194, (byte) 135 });
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static void handshake(Scanner scanner, OutputStream out) throws NoSuchAlgorithmException, IOException {
        String data = scanner.useDelimiter("\\r\\n\\r\\n").next();
        Matcher get = Pattern.compile("^GET").matcher(data);
        if (get.find()) {
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();
            byte[] response = (HS_RESPONSE_HEADERS
                    + Base64.getEncoder().encodeToString(
                            MessageDigest.getInstance("SHA-1").digest((match.group(1) + MAGIC_WS).getBytes(StandardCharsets.UTF_8))
            )
                    + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            out.write(response, 0, response.length);
        }
    }

    private static void decode(byte[] encodedMessage) {
        byte[] decoded = new byte[6];
        byte[] key = new byte[] { (byte) 167, (byte) 225, (byte) 225, (byte) 210 };
        for (int i = 0; i < encodedMessage.length; i++) {
            decoded[i] = (byte) (encodedMessage[i] ^ key[i & 0x3]);
        }
    }

}
