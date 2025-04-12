package com.xxl.tool.test.response;

import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseCode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResponseTest {
    private static final Logger logger = LoggerFactory.getLogger(ResponseTest.class);

    @Test
    public void testResponse() {
        Assertions.assertEquals(Response.ofSuccess().getCode(), ResponseCode.CODE_200.getCode());
        Assertions.assertEquals(Response.ofSuccess("hello").getCode(), ResponseCode.CODE_200.getCode());
        Assertions.assertEquals(Response.ofSuccess("hello").getData(), "hello");

        Assertions.assertEquals(Response.ofFail().getCode(), ResponseCode.CODE_203.getCode());
        Assertions.assertEquals(Response.ofFail("hello").getMsg(), "hello");

        Response result3 = Response.of(200, "message", "hello");
        Assertions.assertEquals(result3.getCode(), 200);
        Assertions.assertEquals(result3.getMsg(), "message");
        Assertions.assertEquals(result3.getData(), "hello");
    }

}
