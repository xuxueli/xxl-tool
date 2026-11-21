package com.xxl.tool.pipeline;

import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Pipeline（处理链）
 *
 * <p>按添加顺序串联多个 {@link PipelineHandler}，每个 Handler 共享同一个
 * {@link PipelineContext}，可读取请求、写入响应与上下文数据，并通过
 * {@link PipelineContext#setBreak(boolean)} / {@code breakToFail} 提前终止链路。</p>
 *
 * <p>执行顺序：按 {@link #addLast}/{@link #addLasts} 的添加顺序依次执行；一旦上下文标记为
 * break 则不再执行后续 Handler。Handler 抛出的异常会被捕获并转换为失败响应，链路随即终止，
 * 不再向调用方抛出。</p>
 *
 * <p>每次 {@link #process} 都会创建独立的上下文，因此构建完成后可并发执行；
 * 但 {@link #getHandlerList()} 暴露的是内部可变列表，构建完成后不应再修改，否则非线程安全。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class Pipeline {
    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

    /**
     * pipeline name
     */
    private String name;
    /**
     * pipeline status
     */
    private String status = PipelineStatus.RUNTIME.getStatus();

    /**
     * pipeline chain (with handler) list
     */
    private final List<PipelineChain> handlerList = new ArrayList<>();

    /**
     * 创建空 Pipeline，默认状态为 {@link PipelineStatus#RUNTIME}
     */
    public Pipeline() {
    }

    /**
     * 设置名称
     *
     * @param name   Pipeline 名称
     * @return       当前实例，便于链式调用
     */
    public Pipeline name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 设置状态
     *
     * <p>状态值需与 {@link PipelineStatus#getStatus()} 一致，{@link PipelineExecutor}
     * 仅执行 {@link PipelineStatus#RUNTIME} 状态的 Pipeline；直接调用 {@link #process} 不受状态限制。</p>
     *
     * @param status   状态标识
     * @return         当前实例，便于链式调用
     */
    public Pipeline status(String status) {
        this.status = status;
        return this;
    }

    /**
     * 获取名称
     *
     * @return Pipeline 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 获取状态
     *
     * @return 状态标识
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取处理链列表
     *
     * @return 内部处理链列表（可变引用，请勿在运行时修改）
     */
    public List<PipelineChain> getHandlerList() {
        return handlerList;
    }

    /**
     * 返回 Pipeline 的字符串表示，包含名称、状态与处理链
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "Pipeline{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", handlerList=" + handlerList +
                '}';
    }

    /**
     * 在链尾追加一个 Handler
     *
     * @param pipelineHandler   Handler，不可为 {@code null}
     * @return                  当前实例，便于链式调用
     */
    public Pipeline addLast(PipelineHandler pipelineHandler) {
        PipelineChain pipelineChain = new PipelineChain(pipelineHandler);
        this.handlerList.add(pipelineChain);
        return this;
    }

    /**
     * 按参数顺序在链尾追加多个 Handler
     *
     * @param pipelineHandlers  Handler 数组，不可为 {@code null}；元素不可为 {@code null}
     * @return                  当前实例，便于链式调用
     */
    public Pipeline addLasts(PipelineHandler... pipelineHandlers) {
        for (PipelineHandler pipelineHandler : pipelineHandlers) {
            addLast(pipelineHandler);
        }
        return this;
    }

    /**
     * 执行处理链（不传入外部上下文）
     *
     * @param request   请求对象，可为 {@code null}
     * @return          处理结果；默认成功，Handler 失败或被中断时为失败响应
     */
    public Response<Object> process(Object request){
        return process(request, null);
    }

    /**
     * 执行处理链
     *
     * <p>为本次执行创建独立的 {@link PipelineContext}；{@code contextMap} 不为 {@code null} 时
     * 直接作为上下文 Map（不拷贝），可用于在多个 Handler 之间及调用方之间共享数据。</p>
     *
     * <p>Handler 顺序执行；任一 Handler 抛出异常时记录日志，将上下文标记中断并写入
     * {@code Response.ofFail(e.getMessage())}，后续 Handler 不再执行，异常不会向调用方抛出。
     * 执行结束后若响应为 {@code null}，则替换为失败响应。</p>
     *
     * @param request       请求对象，可为 {@code null}
     * @param contextMap    外部上下文 Map，可为 {@code null}；非空时要求线程安全（推荐 {@link ConcurrentMap}）
     * @return              处理结果，不会返回 {@code null}
     */
    public Response<Object> process(Object request, ConcurrentMap<String, Object> contextMap) {
        logger.debug("pipeline process start, name:{}, status:{}", name, status);

        // build context
        PipelineContext pipelineContext = new PipelineContext(request);
        if (contextMap != null) {
            pipelineContext.setContextMap(contextMap);
        }

        // process chain handler
        for (PipelineChain handlerChain : handlerList) {
            if (pipelineContext.isBreak()) {
                break;
            }
            try {
                handlerChain.handle(pipelineContext);
            } catch (Exception e) {
                logger.error("pipeline process error, name:{}, pipelineContext:{}", name, pipelineContext, e);
                pipelineContext.breakToFail(Response.ofFail(e.getMessage()));
                //throw new RuntimeException(e);
            }
        }

        // valid response
        if (pipelineContext.getResponse() == null) {
            pipelineContext.breakToFail(Response.ofFail("pipeline response not found."));
        }

        logger.error("pipeline process end, name:{}, pipelineContext:{}", name, pipelineContext);
        return pipelineContext.getResponse();
    }


}
