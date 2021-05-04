package nl.robertlemmens.application;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientSocketConnection extends Thread {

    private static int FIN = 0b1000_0000;
    private static int TEXT_OPCODE = 0b0000_0001;
    private static int PING_OPCODE = 0x9;
    private static int PONG_OPCODE = 0xA;
    private static int MASK = 0x8;
    private static int EXTENDED_LEN_16 = 0x7E;
    private static int EXTENDED_LEN_64 = 0x7F;

    protected Socket socket;

    public ClientSocketConnection(Socket clientSocket) {
        this.socket = clientSocket;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            // Coult not initialize streams. Kill connection.
            e.printStackTrace();
            return;
        }

        Scanner s = new Scanner(in, StandardCharsets.UTF_8);
        try {
            handshake(s, out, in);
            System.out.println("Connected!");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // start client loop
        while (true) {
            System.out.println("Reading...");
            try {
                // Get FIN and OPCODE (only support text)
                int finOpcode = in.read();
                int lenByte = in.read();
                if ( (lenByte - 128) >= 126) {
                    System.out.println("extended payload not supported");
                    break;
                }
                int len = lenByte - 128;
                byte[] keys = new byte[4];
                for (int i = 0; i < 4; i++) {
                    keys[i] = (byte) in.read();
                }

                byte[] message = new byte[len];
                for (int i = 0; i < len; i++) {
                    message[i] = (byte) in.read();
                }

                String decodedMessage = decode(message, keys);
                System.out.println("Client sent: " + decodedMessage);
                System.out.println("Sending back: " + decodedMessage);
                send(decodedMessage, out);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handshake(Scanner scanner, OutputStream out, InputStream in) throws NoSuchAlgorithmException, IOException {
        System.out.println("Handshaking...");
        String data = scanner.useDelimiter("\\r\\n\\r\\n").next();
        Matcher get = Pattern.compile("^GET").matcher(data);
        if (get.find()) {
            Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(data);
            match.find();
            byte[] response = (WebSocketServer.HS_RESPONSE_HEADERS
                    + Base64.getEncoder().encodeToString(
                    MessageDigest.getInstance("SHA-1").digest((match.group(1) + WebSocketServer.MAGIC_WS).getBytes(StandardCharsets.UTF_8))
            )
                    + "\r\n\r\n").getBytes(StandardCharsets.UTF_8);
            out.write(response, 0, response.length);
            out.flush();
        }
    }

    private String decode(byte[] encodedMessage, byte[] keys) {
        byte[] decoded = new byte[encodedMessage.length];
        for (int i = 0; i < encodedMessage.length; i++) {
            decoded[i] = (byte) (encodedMessage[i] ^ keys[i & 0x3]);
        }
        return new String(decoded);
    }

    private void send(String message, OutputStream out) {
        byte finop = (byte) (FIN | TEXT_OPCODE);
        byte length = (byte) (message.length());
        if ( (length) > 125) {
            System.out.println("Extended payload not supported");
        }

        byte[] response = new byte[1 + 1 + message.length()];
        response[0] = finop;
        response[1] = length;
        for(int i = 0; i < message.length(); i++) {
            response[i+2] = message.getBytes()[i];
        }
        try {
            out.write(response, 0, response.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
