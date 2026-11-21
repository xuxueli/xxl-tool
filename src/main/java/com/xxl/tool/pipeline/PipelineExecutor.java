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
 * Pipeline 执行器
 *
 * <p>维护“名称 → {@link Pipeline}”注册表，按名称查找并执行 Pipeline。注册表基于
 * {@link ConcurrentHashMap}，注册与执行可并发进行；Pipeline 自身的并发语义见
 * {@link Pipeline}。</p>
 *
 * <p>执行前会做名称、存在性与状态校验：名称为空白、未注册或状态不是
 * {@link PipelineStatus#RUNTIME} 时不会执行，直接返回 {@code null}（不返回失败响应）。</p>
 *
 * <p>无后台线程，无 start/stop/close 生命周期。</p>
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
     * 注册 Pipeline
     *
     * <p>以 {@link Pipeline#getName()} 为键存入注册表，同名 Pipeline 会覆盖已注册实例；
     * 原实例上已添加的 Handler 不会被合并。</p>
     *
     * @param pipeline  待注册的 Pipeline，可为 {@code null}
     * @return          {@code null}、Handler 列表为空时返回 {@code false}；注册成功返回 {@code true}
     * @throws NullPointerException Pipeline 名称为 {@code null} 时抛出
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
     * 按名称执行 Pipeline
     *
     * @param pipelineName   Pipeline 名称
     * @param request        请求对象，可为 {@code null}
     * @return               处理结果；名称空白、Pipeline 不存在、状态非运行中时返回 {@code null}
     */
    public Response<Object> execute(String pipelineName, Object request) {
        return execute(pipelineName, request, null);
    }

    /**
     * 按名称执行 Pipeline，并传入共享上下文 Map
     *
     * @param pipelineName   Pipeline 名称
     * @param request        请求对象，可为 {@code null}
     * @param contextMap     上下文 Map，可为 {@code null}；非空时要求线程安全
     * @return               处理结果；名称空白、Pipeline 不存在、状态非运行中时返回 {@code null}
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
