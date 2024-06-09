package com.xxl.tool.test.pipeline;

import com.xxl.tool.pipeline.Pipeline;
import com.xxl.tool.pipeline.PipelineHandler;
import com.xxl.tool.pipeline.PipelineStatus;
import com.xxl.tool.response.Response;
import com.xxl.tool.response.ResponseCode;
import com.xxl.tool.test.pipeline.handler.Handler1;
import com.xxl.tool.test.pipeline.handler.Handler2;
import com.xxl.tool.test.pipeline.handler.Handler3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineTest {
    private static final Logger logger = LoggerFactory.getLogger(PipelineTest.class);

    @Test
    public void testPipeline() {
        // handler
        PipelineHandler handler1 = new Handler1();
        PipelineHandler handler2 = new Handler2();
        PipelineHandler handler3 = new Handler3();

        // build pipeline
        Pipeline p1 = new Pipeline()
                .name("p1")
                .status(PipelineStatus.RUNTIME.getStatus())
                .addLasts(handler1, handler2, handler3);

        // make request
        DemoRequest requet = new DemoRequest("abc", 100);

        // process
        Response<Object>  response = p1.process(requet);

        logger.info("response: {}", response);
        Assertions.assertEquals(response.getCode(), ResponseCode.CODE_200.getCode());
    }

    public static class DemoRequest{
        private String arg1;
        private int arg2;

        public DemoRequest() {
        }
        public DemoRequest(String arg1, int arg2) {
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
    }

}
