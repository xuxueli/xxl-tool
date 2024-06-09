package com.xxl.tool.test.pipeline.handler;

import com.xxl.tool.pipeline.PipelineContext;
import com.xxl.tool.pipeline.PipelineHandler;
import com.xxl.tool.test.pipeline.PipelineTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler1 extends PipelineHandler {
    private static final Logger logger = LoggerFactory.getLogger(Handler1.class);

    @Override
    public void handle(PipelineContext pipelineContext) {
        PipelineTest.DemoRequest request = (PipelineTest.DemoRequest) pipelineContext.getRequest();
        logger.info("Handler1 run:", request.getArg1());
    }
}
