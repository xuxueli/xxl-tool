//package com.xxl.tool.test.deprecated.httprpc;
//
//import com.xxl.tool.deprecated.httprpc.HttpRpcClient;
//import com.xxl.tool.response.Response;
//import org.junit.jupiter.api.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class HttpRpcTest {
//
//    // ------------------------------------------------------------ httprpc ------------------------------------------------------------
//
//    // auth
//    @Test
//    public void test4() {
//        DemoService client = HttpRpcClient.create()
//                .url("http://localhost:8080/jsonrpc")
//                .servicePath("/demoService")
//                .auth("1234562")
//                .timeout(60*1000)
//                .proxy(DemoService.class);
//
//        System.out.println("------------------------");
//        System.out.println("test: " + client.test(new DemoReq("jack1")));
//    }
//
//    // 不同 实现
//    @Test
//    public void test3() {
//        DemoService client = HttpRpcClient.create().url("http://localhost:8080/jsonrpc3").servicePath("/demoService3").timeout(100000).proxy(DemoService.class);
//
//        System.out.println("------------------------");
//        System.out.println("test: " + client.test(new DemoReq("jack1")));
//        System.out.println("test2: " + client.test2(new DemoReq("jack2")));
//        System.out.println("test3: " + client.test3(new DemoReq("jack3"), new DemoReq("jack22")));      // springboot，只支持一个 body 对象；
//        client.a11111();
//        System.out.println("test4: run");
//    }
//
//    // 不同 service
//    @Test
//    public void test2() {
//        DemoService demoService = HttpRpcClient.create().url("http://localhost:8080/jsonrpc").servicePath("/demoService").proxy(DemoService.class);
//        System.out.println("test: " + demoService.test(new DemoReq("jack")));
//
//        DemoService demoService2 = HttpRpcClient.create().url("http://localhost:8080/jsonrpc").servicePath("/demoService2").proxy(DemoService.class);
//        System.out.println("test: " + demoService2.test(new DemoReq("jack")));
//    }
//
//    // 不同 client
//    @Test
//    public void test1() {
//        List<DemoService> clientList = new ArrayList<>();
//        clientList.add(HttpRpcClient.create().proxy(DemoService.class));
//        clientList.add(HttpRpcClient.create().url("http://localhost:8080/jsonrpc").proxy(DemoService.class));
//        clientList.add(HttpRpcClient.create().servicePath("/demoService").proxy(DemoService.class));
//        clientList.add(HttpRpcClient.create().timeout(3000).proxy(DemoService.class));
//        clientList.add(HttpRpcClient.create().url("http://localhost:8080/jsonrpc").servicePath("/demoService").timeout(3000).proxy(DemoService.class));
//
//        for (DemoService client : clientList) {
//            System.out.println("------------------------");
//            System.out.println("test: " + client.test(new DemoReq("jack")));
//            System.out.println("test2: " + client.test2(new DemoReq("jack")));
//            System.out.println("test3: " + client.test3(new DemoReq("jack"), new DemoReq("jack22")));
//            client.a11111();
//            System.out.println("test4: run");
//        }
//    }
//
//    // --------------------------------- iface ---------------------------------
//
//    public static class DemoReq {
//        private String name;
//
//        public DemoReq() {
//        }
//
//        public DemoReq(String name) {
//            this.name = name;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//    }
//
//    public class DemoResp {
//
//        private String name;
//        private String remark;
//
//        public DemoResp() {
//        }
//
//        public DemoResp(String name, String remark) {
//            this.name = name;
//            this.remark = remark;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getRemark() {
//            return remark;
//        }
//
//        public void setRemark(String remark) {
//            this.remark = remark;
//        }
//
//        @Override
//        public String toString() {
//            return "DemoResp{" +
//                    "name='" + name + '\'' +
//                    ", remark='" + remark + '\'' +
//                    '}';
//        }
//    }
//
//    @HttpRpcClient.HttpRpcService(url = "http://localhost:8080/jsonrpc", path = "/demoService", timeout = 3000)
//    public interface DemoService {
//
//        public DemoResp test(DemoReq req);
//
//        public Response<DemoResp> test2(DemoReq req);
//
//        public Response<DemoResp> test3(DemoReq req, DemoReq req2);
//
//        @HttpRpcClient.HttpRpcMethod(path = "/test4", timeout = 5000)
//        public void a11111();
//
//    }
//
//}