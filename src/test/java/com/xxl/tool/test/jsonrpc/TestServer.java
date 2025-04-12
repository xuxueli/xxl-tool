package com.xxl.tool.test.jsonrpc;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.xxl.tool.io.IOTool;
import com.xxl.tool.jsonrpc.JsonRpcServer;
import com.xxl.tool.test.jsonrpc.service.impl.UserServiceImpl;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TestServer {

    public static void main(String[] args) throws IOException {

        // create http server
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // create context with path
        server.createContext("/jsonrpc", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                // valid
                if (!("POST".equalsIgnoreCase(httpExchange.getRequestMethod()) && "/jsonrpc".equals(httpExchange.getRequestURI().getPath()))) {
                    writeResponse(httpExchange, "not support method!");
                    return;
                }

                // exchange and invoke
                String requestBody = IOTool.copyToString(httpExchange.getRequestBody(), Charset.defaultCharset());
                System.out.println("\n\n requestBody = " + requestBody);

                String jsonRpcResponse = jsonRpcServer.invoke(requestBody);
                System.out.println("jsonRpcResponse = " + jsonRpcResponse);

                writeResponse(httpExchange, jsonRpcResponse);

            }
        });

        server.createContext("/", httpExchange -> writeResponse(httpExchange, "Hello World."));

        // 启动服务器
        server.start();
        System.out.println("Server is running on port 8080");
    }

    /**
     * init json-rpc server
     */
    private static JsonRpcServer jsonRpcServer = new JsonRpcServer();
    static {
        jsonRpcServer.register("userService", new UserServiceImpl());
    }

    /**
     * write to httpclient
     *
     * @param httpExchange
     * @param response
     * @throws IOException
     */
    private static void writeResponse(HttpExchange httpExchange, String response) throws IOException {
        // write response
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes(StandardCharsets.UTF_8));
        os.close();
    }

    /*@Test
    public void createUserTest(){
        // request
        JsonRpcRequest request = new JsonRpcRequest(
                "userService",
                "createUser",
                new JsonElement[]{
                        GsonTool.toJsonElement(new UserDTO("jack", 18))
                });

        // invoke
        JsonRpcResponse response = jsonRpcServer.invoke(request);
        System.out.println("serverInvoke：response = " + GsonTool.toJson(response));
    }*/

}
