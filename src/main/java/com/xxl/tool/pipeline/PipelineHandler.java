package com.xxl.tool.pipeline;

/**
 * pipeline handler
 *
 * @author xuxueli 2024-01-01
 */
public abstract class PipelineHandler {

    /**
     * do handle
     *
     * @param pipelineContext
     */
    public abstract void handle(PipelineContext pipelineContext);

}
