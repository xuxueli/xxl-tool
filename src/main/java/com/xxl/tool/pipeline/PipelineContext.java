package com.xxl.tool.pipeline;

import com.xxl.tool.response.Response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Pipeline 上下文
 *
 * <p>保存单次 Pipeline 执行的请求、响应、中断标记以及可在 Handler 之间共享的数据 Map。
 * 每个 {@link Pipeline#process} 调用都会创建独立实例，不同执行之间互不影响。</p>
 *
 * <p>响应默认初始化为成功（{@code Response.ofSuccess()}）；中断标记默认 {@code false}。
 * 上下文本身非线程安全，但默认的 {@link #getContextMap()} 为 {@link ConcurrentHashMap}，
 * 同一执行内跨线程读写 Map 时仍建议使用线程安全容器，且不应依赖 {@code request}/{@code response}
 * 的跨线程可见性。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineContext {

    /**
     * Pipeline 请求
     */
    private Object request;
    /**
     * Pipeline 响应，默认成功
     */
    private Response<Object> response = Response.ofSuccess();
    /**
     * 是否中断后续 Handler
     */
    private boolean isBreak = false;
    /**
     * Handler 间共享的上下文数据
     */
    private ConcurrentMap<String, Object> contextMap = new ConcurrentHashMap<>();

    /**
     * 创建空上下文，响应默认为成功
     */
    public PipelineContext() {
    }

    /**
     * 创建带请求的上下文，响应默认为成功
     *
     * @param request   请求对象，可为 {@code null}
     */
    public PipelineContext(Object request) {
        this.request = request;
    }

    /**
     * 获取请求
     *
     * @return 请求对象，可能为 {@code null}
     */
    public Object getRequest() {
        return request;
    }

    /**
     * 设置请求
     *
     * @param request   请求对象，可为 {@code null}
     */
    public void setRequest(Object request) {
        this.request = request;
    }

    /**
     * 获取响应
     *
     * @return 响应，默认成功；可能被 Handler 设置为 {@code null}
     */
    public Response<Object> getResponse() {
        return response;
    }

    /**
     * 设置响应
     *
     * @param response   响应对象，可为 {@code null}
     */
    public void setResponse(Response<Object> response) {
        this.response = response;
    }

    /**
     * 是否已中断后续 Handler
     *
     * @return 已中断返回 {@code true}
     */
    public boolean isBreak() {
        return isBreak;
    }

    /**
     * 设置中断标记
     *
     * <p>仅标记中断，不改变响应；需要同时返回失败时使用 {@link #breakToFail}。</p>
     *
     * @param aBreak   是否中断
     */
    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    /**
     * 获取共享上下文数据
     *
     * @return 上下文 Map，默认非 {@code null}；若被 {@link #setContextMap} 置空则可能为 {@code null}
     */
    public ConcurrentMap<String, Object> getContextMap() {
        return contextMap;
    }

    /**
     * 设置共享上下文数据（直接替换引用，不做拷贝）
     *
     * @param contextMap   上下文 Map，可为 {@code null}；跨线程使用时需保证线程安全
     */
    public void setContextMap(ConcurrentMap<String, Object> contextMap) {
        this.contextMap = contextMap;
    }

    /**
     * 返回上下文的字符串表示，包含请求、响应、中断标记与共享数据
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return "PipelineContext{" +
                "request=" + request +
                ", response=" + response +
                ", isBreak=" + isBreak +
                ", contextMap=" + contextMap +
                '}';
    }

    /**
     * 中断 Pipeline 并返回默认失败响应
     */
    public void breakToFail(){
        breakToFail(Response.ofFail());
    }

    /**
     * 中断 Pipeline 并返回指定失败响应
     *
     * @param response   失败响应，调用方应传入失败结果
     */
    public void breakToFail(Response<Object> response) {
        this.isBreak = true;
        this.response = response;
    }

}
