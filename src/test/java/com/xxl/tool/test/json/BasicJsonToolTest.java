package com.xxl.tool.test.json;

import com.xxl.tool.json.BasicJsonTool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BasicJsonToolTest {

    public static void main(String[] args) {
        Map<String, Object> result = new HashMap<>();
        result.put("int", 200);
        result.put("str", "success");
        result.put("arr", Arrays.asList("111","222"));
        result.put("float", 1.11f);

        String json = BasicJsonTool.toJson(result);
        System.out.println(json);

        Object objectMap = BasicJsonTool.parseMap(json);
        System.out.println(objectMap);
    }

}
