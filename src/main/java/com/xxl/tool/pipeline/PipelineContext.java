package com.xxl.tool.pipeline;

import com.xxl.tool.response.Response;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * pipeline context
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineContext {

    /**
     * pipelint requeust
     */
    private Object request;
    /**
     * pipelint response
     */
    private Response<Object> response = Response.ofSuccess();
    /**
     * is break or not
     */
    private boolean isBreak = false;
    /**
     * pipeline context map
     */
    private ConcurrentMap<String, Object> contextMap = new ConcurrentHashMap<>();

    public PipelineContext() {
    }
    public PipelineContext(Object request) {
        this.request = request;
    }

    public Object getRequest() {
        return request;
    }

    public void setRequest(Object request) {
        this.request = request;
    }

    public Response<Object> getResponse() {
        return response;
    }

    public void setResponse(Response<Object> response) {
        this.response = response;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public void setBreak(boolean aBreak) {
        isBreak = aBreak;
    }

    public ConcurrentMap<String, Object> getContextMap() {
        return contextMap;
    }

    public void setContextMap(ConcurrentMap<String, Object> contextMap) {
        this.contextMap = contextMap;
    }

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
     * brank pipeline and response fail
     */
    public void breakToFail(){
        breakToFail(Response.ofFail());
    }

    /**
     * brank pipeline and response fail
     *
     * @param response
     */
    public void breakToFail(Response<Object> response) {
        this.isBreak = true;
        this.response = response;
    }

}
