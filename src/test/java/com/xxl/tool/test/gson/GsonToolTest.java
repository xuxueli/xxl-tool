package com.xxl.tool.test.gson;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.xxl.tool.gson.GsonTool;
import com.xxl.tool.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GsonToolTest {
    private static final Logger logger = LoggerFactory.getLogger(GsonToolTest.class);

    @Test
    public void testJson() {
        Map<String, Object> result = new HashMap<>();
        result.put("int", 200);
        result.put("str", "success");
        result.put("arr", Arrays.asList("111","222"));
        result.put("float", 1.11f);

        String json = GsonTool.toJson(result);
        logger.info(json);

        Object objectMap = GsonTool.fromJson(json, Map.class);
        logger.info(objectMap.toString());
    }

    @Test
    public void testFromJsonList(){

        Demo demo = new Demo("abc", 100);
        String json = GsonTool.toJson(demo);
        logger.info(json);

        Demo demo2 = GsonTool.fromJson(json, Demo.class);
        logger.info(demo2.toString());

        Assertions.assertEquals(json, GsonTool.toJson(demo2));
    }

    @Test
    public void testType(){

        Response<DemoResult> originData = Response.ofSuccess();
        originData.setData(new DemoResult(Arrays.asList(new Demo("abc", 100), new Demo("def", 200))));

        JsonElement data1 = GsonTool.toJsonElement(originData);
        String json = GsonTool.toJson(data1);
        logger.info("data1 = "+json);

        Response data2 = GsonTool.fromJsonElement(data1, Response.class, DemoResult.class);
        logger.info("data2 = "+data2);

        Assertions.assertEquals(json, GsonTool.toJson(data2));
    }

    @Test
    public void testType2(){

        Response<List<Demo>> originData = Response.ofSuccess();
        originData.setData(Arrays.asList(new Demo("abc", 100), new Demo("def", 200)));

        JsonElement data1 = GsonTool.toJsonElement(originData);
        String json = GsonTool.toJson(data1);
        logger.info("data1 = "+json);

        Response data2 = GsonTool.fromJsonElement(data1, Response.class, List.class);
        logger.info("data2 = "+data2);

        Assertions.assertEquals(json, GsonTool.toJson(data2));
    }

    public static class DemoResult{
        private List<Demo> demoList;

        public DemoResult(List<Demo> demoList) {
            this.demoList = demoList;
        }

        public List<Demo> getDemoList() {
            return demoList;
        }

        public void setDemoList(List<Demo> demoList) {
            this.demoList = demoList;
        }
    }

    public static class Demo{
        private String arg1;
        private int arg2;

        public Demo() {
        }
        public Demo(String arg1, int arg2) {
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        public String getArg1() {
            return arg1;
        }

        public void setArg1(String arg1) {
            this.arg1 = arg1;
        }

        public int getArg2() {
            return arg2;
        }

        public void setArg2(int arg2) {
            this.arg2 = arg2;
        }

        @Override
        public String toString() {
            return "Demo{" +
                    "arg1='" + arg1 + '\'' +
                    ", arg2=" + arg2 +
                    '}';
        }

    }

}
