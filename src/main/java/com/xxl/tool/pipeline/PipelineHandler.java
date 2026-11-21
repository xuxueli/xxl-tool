package com.xxl.tool.pipeline;

/**
 * Pipeline 处理器
 *
 * <p>Pipeline 的最小执行单元。Handler 按注册顺序执行，共享同一个 {@link PipelineContext}：
 * 可从上下文读取请求与共享数据，向 {@link PipelineContext#setResponse} 写入结果，
 * 或调用 {@code breakToFail} 提前终止链路。</p>
 *
 * <p>Handler 抛出的异常会被 {@link Pipeline} 捕获并转换为失败响应，不会继续执行后续 Handler。
 * 实现类应自行保证线程安全：同一个 Handler 实例可能被并发执行。</p>
 *
 * @author xuxueli 2024-01-01
 */
public abstract class PipelineHandler {

    /**
     * 处理请求
     *
     * <p>通过 {@code pipelineContext} 读取请求与共享数据，并写入响应或中断标记。
     * 未写入响应时沿用上下文默认的成功响应；抛出异常时链路以失败结束。</p>
     *
     * @param pipelineContext   本次执行的上下文，不可为 {@code null}
     */
    public abstract void handle(PipelineContext pipelineContext);

}
