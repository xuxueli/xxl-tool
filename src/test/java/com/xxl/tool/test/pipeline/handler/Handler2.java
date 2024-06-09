package com.xxl.tool.test.pipeline.handler;

import com.xxl.tool.pipeline.PipelineContext;
import com.xxl.tool.pipeline.PipelineHandler;
import com.xxl.tool.test.pipeline.PipelineTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler2 extends PipelineHandler {
    private static final Logger logger = LoggerFactory.getLogger(Handler2.class);

    @Override
    public void handle(PipelineContext pipelineContext) {
        PipelineTest.DemoRequest request = (PipelineTest.DemoRequest) pipelineContext.getRequest();

        if ("jack".equals(request.getArg1())) {
            logger.info("Handler2 error:", request.getArg1());
            pipelineContext.breakToFail();
            return;
        }

        logger.info("Handler2 run:", request.getArg1());
    }
}
