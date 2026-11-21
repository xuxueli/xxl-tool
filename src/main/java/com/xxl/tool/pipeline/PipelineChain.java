package com.xxl.tool.pipeline;

/**
 * Pipeline 处理链节点
 *
 * <p>包装单个 {@link PipelineHandler}，由 {@link Pipeline} 按添加顺序逐个调用。
 * 节点本身无状态，线程安全性取决于所包装的 Handler。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineChain {

    private final PipelineHandler pipelineHandler;

    /**
     * 创建处理链节点
     *
     * @param pipelineHandler   被包装的 Handler，不可为 {@code null}
     */
    public PipelineChain(PipelineHandler pipelineHandler) {
        this.pipelineHandler = pipelineHandler;
    }

    /**
     * 执行所包装的 Handler
     *
     * <p>Handler 抛出的异常会原样向上传播，由 {@link Pipeline} 统一捕获处理。</p>
     *
     * @param pipelineContext   本次执行的上下文，不可为 {@code null}
     */
    public void handle(PipelineContext pipelineContext) {
        this.pipelineHandler.handle(pipelineContext);
    }

}
