package com.xxl.tool.test.jsonrpc;

import com.xxl.tool.jsonrpc.JsonRpcClient;
import com.xxl.tool.response.Response;
import com.xxl.tool.test.jsonrpc.service.UserService;
import com.xxl.tool.test.jsonrpc.service.model.ResultDTO;
import com.xxl.tool.test.jsonrpc.service.model.UserDTO;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

public class TestClient {

    private static String url = "http://localhost:8080/jsonrpc";
    private static String service = "userService";

    /**
     * build client
     *
     * @return
     */
    private JsonRpcClient buildClient(){
        // proxy
        return JsonRpcClient
                .newClient()
                .url( url)
                .timeout(3 * 1000);
    }

    @Test
    public void loadUserTest() {
        UserService userService = buildClient().proxy(service, UserService.class);

        // proxy
        UserDTO result = userService.loadUser("zhangsan");
        System.out.println("proxy invoke, result = " + result);

        // invoke
        JsonRpcClient jsonRpcClient = buildClient();
        UserDTO result2 = jsonRpcClient.invoke(
                service,
                "loadUser",
                new Object[]{
                        "zhangsan"
                },
                UserDTO.class);
        System.out.println("client invoke, result2 = " + result2);
    }

    @Test
    public void queryUserByAgeTest() {
        UserService userService = buildClient().proxy(service, UserService.class);

        // proxy
        List<UserDTO> result = userService.queryUserByAge();
        System.out.println("proxy invoke, result = " + result);

        // invoke
        JsonRpcClient jsonRpcClient = buildClient();
        List<UserDTO> result2 = jsonRpcClient.invoke(
                "userService",
                "queryUserByAge",
                null,
                List.class);
        System.out.println("client invoke, result2 = " + result2);
    }

    @Test
    public void createUserTest() {
        UserService userService = buildClient().proxy(service, UserService.class);

        // proxy
        ResultDTO result = userService.createUser(new UserDTO("jack", 28));
        System.out.println("proxy result = " + result);

        // invoke
        JsonRpcClient jsonRpcClient = buildClient();
        ResultDTO result2 = jsonRpcClient.invoke(
                "userService",
                "createUser",
                new Object[]{
                        new UserDTO("jack", 28)
                },
                ResultDTO.class);

        System.out.println("client invoke, result2 = " + result2);
    }

    @Test
    public void updateUserTest() {
        UserService userService = buildClient().proxy(service, UserService.class);

        // proxy
        ResultDTO result = userService.updateUser("jack", 28);
        System.out.println("proxy result = " + result);

        // invoke
        JsonRpcClient jsonRpcClient = buildClient();
        ResultDTO result2 = jsonRpcClient.invoke(
                "userService",
                "updateUser",
                new Object[]{
                        "jack",
                        28
                },
                ResultDTO.class);
        System.out.println("client invoke, result2 = " + result2);
    }

    @Test
    public void refreshTest() {
        UserService userService = buildClient().proxy(service, UserService.class);

        // proxy
        userService.refresh();
        System.out.println("proxy refresh");

        // invoke
        JsonRpcClient jsonRpcClient = buildClient();
        ResultDTO result2 = jsonRpcClient.invoke(
                "userService",
                "refresh",
                null,
                null);
        System.out.println("client invoke, result2 = " + result2);
    }

    @Test
    public void loadTest() {
        UserService userService = buildClient().header("token", "12345678").proxy(service, UserService.class);

        // proxy
        Response<UserDTO> result = userService.load("jack");
        System.out.println("proxy result = " + result);

        // invoke
        JsonRpcClient jsonRpcClient = buildClient();
        Response<UserDTO> result2 = jsonRpcClient.invoke(
                "userService",
                "load",
                new Object[]{
                        "jack"
                },
                Response.class,
                new Type[]{
                        UserDTO.class
                });
        System.out.println("client invoke, result2 = " + result2);
    }

    @Test
    public void test1() {
        UserService userService = buildClient().header("token", "12345678").proxy(service, UserService.class);

        long start = System.currentTimeMillis();
        for (long i = 0; i < 10000L * 10000; i++) {
            if (i % 10000 == 0) {
                System.out.println(i + "= " + userService.load("jack"));
            }
        }
        long cost = System.currentTimeMillis() - start;
        System.out.println("cost = " + cost);

    }
}
