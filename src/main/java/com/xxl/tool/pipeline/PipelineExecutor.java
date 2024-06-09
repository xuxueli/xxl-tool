package com.xxl.tool.pipeline;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * pipeline executor
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineExecutor {
    private static final Logger logger = LoggerFactory.getLogger(PipelineExecutor.class);

    /**
     * pipeline store
     */
    private final Map<String, Pipeline> pipelineMap = new ConcurrentHashMap<>();

    /**
     * registry pipeline
     *
     * @param pipeline
     * @return
     */
    public boolean registry(Pipeline pipeline) {
        // valid
        if (pipeline == null) {
            logger.debug("PipelineExecutor registry fail, pipeline is null");
            return false;
        }
        if (CollectionTool.isEmpty(pipeline.getHandlerList())) {
            logger.debug("PipelineExecutor registry fail, pipeline[{}] is empty", pipeline);
            return false;
        }
        // registry
        pipelineMap.put(pipeline.getName(), pipeline);
        return true;
    }

    /**
     * do execute
     *
     * @param pipelineName
     * @param request
     * @return
     */
    public Response<Object> execute(String pipelineName, Object request) {
        return execute(pipelineName, request, null);
    }

    /**
     * do execute
     *
     * @param pipelineName
     * @param request
     * @param contextMap
     * @return
     */
    private Response<Object> execute(String pipelineName, Object request, ConcurrentMap<String, Object> contextMap) {
        // valid and match
        if (StringTool.isBlank(pipelineName)) {
            logger.debug("PipelineExecutor execute fail, pipelineName is null");
            return null;
        }
        Pipeline pipeline = pipelineMap.get(pipelineName);
        if (pipeline == null) {
            logger.debug("PipelineExecutor execute fail, pipeline[{}] is null", pipelineName);
            return null;
        }
        if (PipelineStatus.RUNTIME != PipelineStatus.findByStatus(pipeline.getStatus())){
            logger.debug("PipelineExecutor execute fail, pipeline[{}] is not running", pipelineName);
            return null;
        }

        // do process
        return pipeline.process(request, contextMap);
    }


}
