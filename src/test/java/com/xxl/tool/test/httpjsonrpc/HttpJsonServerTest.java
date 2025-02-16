package com.xxl.tool.test.httpjsonrpc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xxl.tool.httpjsonrpc.HttpJsonServerTool;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

public class HttpJsonServerTest {

    public static void main(String[] args) throws IOException {
        // build server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // add handler
        server.createContext("/get", new GetHandler());
        server.createContext("/post", new PostHandler());

        // set executor
        server.setExecutor(null); // 使用默认的线程池

        // start server
        server.start();
        System.out.println("Server started on port 8080");
    }

    static class PostHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // valid method
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                return;
            }

            // read request
            String requestBody = getRequestBody(exchange);

            // dispatch and invoke
            String response = HttpJsonServerTool.invoke("fun01", requestBody);
            System.out.println("request: " + requestBody + "\nresponse: " + response);


            // write response
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes(StandardCharsets.UTF_8));
            os.close();
        }
    }

    // mock build function
    static {
        HttpJsonServerTool.addFunction("fun01", new Function<HttpJsonToolTest.DemoRequest, HttpJsonToolTest.DemoResponse>() {
            @Override
            public HttpJsonToolTest.DemoResponse apply(HttpJsonToolTest.DemoRequest t) {
                return new HttpJsonToolTest.DemoResponse(t.getName(), 18, "Hi " + t.getName());
            }
        });
    }
    private static String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder body = new StringBuilder();
        byte[] buffer = new byte[1024]; // Adjust the buffer size as needed
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            body.append(new String(buffer, 0, bytesRead));
        }
        return body.toString();
    }

    static class GetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            // valid method
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); // 405 Method Not Allowed
                return;
            }

            // build response
            String response = "This is a GET request response";

            // write response
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }


}
