package com.xxl.tool.test.pipeline;

import com.xxl.tool.pipeline.Pipeline;
import com.xxl.tool.pipeline.PipelineExecutor;
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

public class PipelineExecutorTest {
    private static final Logger logger = LoggerFactory.getLogger(PipelineExecutorTest.class);

    @Test
    public void testPipelineExecutor() {
        // handler
        PipelineHandler handler1 = new Handler1();
        PipelineHandler handler2 = new Handler2();
        PipelineHandler handler3 = new Handler3();

        // build pipeline
        Pipeline p1 = new Pipeline()
                .name("p1")
                .status(PipelineStatus.RUNTIME.getStatus())
                .addLasts(handler1, handler2, handler3);

        Pipeline p2 = new Pipeline()
                .name("p2")
                .status(PipelineStatus.RUNTIME.getStatus())
                .addLasts(handler2, handler1, handler3);

        // build executor
        PipelineExecutor executor = new PipelineExecutor();
        executor.registry(p1);
        executor.registry(p2);

        // make request
        PipelineTest.DemoRequest requet1 = new PipelineTest.DemoRequest("aaa", 100);
        PipelineTest.DemoRequest requet2 = new PipelineTest.DemoRequest("bbb", 100);

        // process
        Response<Object> response1 = p1.process(requet1);
        logger.info("response1: {}", response1);
        Assertions.assertEquals(response1.getCode(), ResponseCode.CODE_200.getCode());

        Response<Object>  response2 = p2.process(requet2);
        logger.info("response2: {}", response2);
        Assertions.assertEquals(response2.getCode(), ResponseCode.CODE_200.getCode());
    }


}
