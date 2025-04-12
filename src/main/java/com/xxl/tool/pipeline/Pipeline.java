package com.xxl.tool.pipeline;

import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;

/**
 * Pipeline
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

    public Pipeline() {
    }

    public Pipeline name(String name) {
        this.name = name;
        return this;
    }
    public Pipeline status(String status) {
        this.status = status;
        return this;
    }

    public String getName() {
        return name;
    }
    public String getStatus() {
        return status;
    }
    public List<PipelineChain> getHandlerList() {
        return handlerList;
    }

    @Override
    public String toString() {
        return "Pipeline{" +
                "name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", handlerList=" + handlerList +
                '}';
    }

    /**
     * add a chain to the tail
     *
     * @param pipelineHandler
     * @return
     */
    public Pipeline addLast(PipelineHandler pipelineHandler) {
        PipelineChain pipelineChain = new PipelineChain(pipelineHandler);
        this.handlerList.add(pipelineChain);
        return this;
    }

    /**
     * add chain list to the tail
     *
     * @param pipelineHandlers
     * @return
     */
    public Pipeline addLasts(PipelineHandler... pipelineHandlers) {
        for (PipelineHandler pipelineHandler : pipelineHandlers) {
            addLast(pipelineHandler);
        }
        return this;
    }

    /**
     * do process
     *
     * @param request
     * @return
     */
    public Response<Object> process(Object request){
        return process(request, null);
    }

    /**
     * do process
     *
     * @param request
     * @param contextMap
     * @return
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
