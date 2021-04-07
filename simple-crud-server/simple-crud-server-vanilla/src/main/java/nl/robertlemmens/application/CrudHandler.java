package nl.robertlemmens.application;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class CrudHandler implements HttpHandler {

    private static final AtomicLong sequence = new AtomicLong();
    private static final ConcurrentHashMap<Long, Person> personMap = new ConcurrentHashMap<>();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        switch (exchange.getRequestMethod()) {
            case "GET":
                handleGet(exchange);
                break;
            case "POST":
                handlePost(exchange);
                break;
            case "PUT":
                handlePut(exchange);
                break;
            case "DELETE":
                handleDelete(exchange);
                break;
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/persons")) {
            OutputStream outputStream = exchange.getResponseBody();
            List<Person> persons = new ArrayList<>(personMap.values());
            exchange.sendResponseHeaders(200, 0);
            outputStream.write("[".getBytes(Charset.defaultCharset()));
            for (int i = 0; i < persons.size(); i++) {
                outputStream.write(persons.get(i).toJson().getBytes(Charset.defaultCharset()));
                if (i < (persons.size() - 1))
                    outputStream.write(",".getBytes(Charset.defaultCharset()));
            }
            outputStream.write("]".getBytes(Charset.defaultCharset()));
            outputStream.flush();
            outputStream.close();
        } else {
            // try parsing as /{id} or fail
            String id = exchange.getRequestURI().getPath().substring(9);
            Person p = personMap.get(Long.parseLong(id));

            OutputStream outputStream = exchange.getResponseBody();
            if (p == null) {
                exchange.sendResponseHeaders(404, 0);
                outputStream.flush();
                outputStream.close();
            } else {
                exchange.sendResponseHeaders(200, 0);
                writeToOutputStream(exchange, p.toJson());
            }
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String jsonBody = new String(exchange.getRequestBody().readAllBytes(), Charset.defaultCharset());
        String name = getStringValueFromJsonByKey("name", jsonBody);
        int age = Integer.parseInt(getStringValueFromJsonByKey("age", jsonBody));
        long id = sequence.getAndIncrement();
        Person p = Person.of(id, name, age);
        personMap.put(id, p);
        exchange.sendResponseHeaders(200, p.toJson().length());
        writeToOutputStream(exchange, p.toJson());
    }

    private void handlePut(HttpExchange exchange) throws IOException {
        String jsonBody = new String(exchange.getRequestBody().readAllBytes(), Charset.defaultCharset());
        String name = getStringValueFromJsonByKey("name", jsonBody);
        int age = Integer.parseInt(getStringValueFromJsonByKey("age", jsonBody));
        long id = Integer.parseInt(getStringValueFromJsonByKey("id", jsonBody));

        personMap.get(id).setName(name);
        personMap.get(id).setAge(age);

        exchange.sendResponseHeaders(200, personMap.get(id).toJson().length());
        writeToOutputStream(exchange, personMap.get(id).toJson());
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring(9);
        boolean result = personMap.remove(Long.parseLong(id)) != null;
        exchange.sendResponseHeaders(200, 0);
        writeToOutputStream(exchange, String.valueOf(result));
    }

    private void writeToOutputStream(HttpExchange exchange, String toWrite) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();
        outputStream.write(toWrite.getBytes(Charset.defaultCharset()));
        outputStream.flush();
        outputStream.close();
    }

    // naive impl to get values from json, only works for our example
    private String getStringValueFromJsonByKey(String key, String json) {
        String[] parts = json.split(",");
        for (int i = 0; i < parts.length; i++) {
            String[] kv = parts[i].replace("{", "").replace("}", "").replaceAll("\"", "").split(":");
            String k = kv[0].trim();
            String v = kv[1].trim();
            System.out.println(v);
            if (key.equals(k)) {
                return v;
            }
        }
        return null;
    }
}
