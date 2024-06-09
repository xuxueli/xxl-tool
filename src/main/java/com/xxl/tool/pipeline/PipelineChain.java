package com.xxl.tool.pipeline;

/**
 * Pipeline Chain
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineChain {

    private final PipelineHandler pipelineHandler;

    public PipelineChain(PipelineHandler pipelineHandler) {
        this.pipelineHandler = pipelineHandler;
    }

    /**
     * do handler
     *
     * @param pipelineContext
     */
    public void handle(PipelineContext pipelineContext) {
        this.pipelineHandler.handle(pipelineContext);
    }

}
