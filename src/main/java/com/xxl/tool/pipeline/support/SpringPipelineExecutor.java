package com.xxl.tool.pipeline.support;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.MapTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.pipeline.Pipeline;
import com.xxl.tool.pipeline.PipelineExecutor;
import com.xxl.tool.pipeline.PipelineHandler;
import com.xxl.tool.pipeline.PipelineStatus;
import com.xxl.tool.pipeline.config.PipelineConfig;
import com.xxl.tool.pipeline.config.PipelineConfigLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;

/**
 * pipeline config loader, local version
 *
 * @author xuxueli 2024-01-01
 */
public class SpringPipelineExecutor extends PipelineExecutor
        implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static final Logger logger = LoggerFactory.getLogger(SpringPipelineExecutor.class);

    private ApplicationContext applicationContext;
    private PipelineConfigLoader pipelineConfigLoader;

    public void setPipelineConfigLoader(PipelineConfigLoader pipelineConfigLoader) {
        this.pipelineConfigLoader = pipelineConfigLoader;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        logger.info("SpringPipelineExecutor init.");
        init();
    }

    /**
     * do init
     *
     * @return
     */
    private boolean init() {
        if (applicationContext == null) {
            logger.info("SpringPipelineExecutor init fail. applicationContext is null.");
            return false;
        }
        if (pipelineConfigLoader == null) {
            logger.info("SpringPipelineExecutor init fail. pipelineConfigLoader is null.");
            return false;
        }
        // load config
        List<PipelineConfig> configList = pipelineConfigLoader.loadAll();
        if (CollectionTool.isEmpty(configList)) {
            logger.info("SpringPipelineExecutor init fail. configList is null.");
            return false;
        }
        // find handlers
        Map<String, PipelineHandler> handlerMap = applicationContext.getBeansOfType(PipelineHandler.class);
        if (MapTool.isEmpty(handlerMap)) {
            logger.info("SpringPipelineExecutor init fail. handlerMap is null.");
            return false;
        }
        // registry pipeline
        for (PipelineConfig config : configList) {
            boolean ret = registryByConfig(config, handlerMap);
            logger.info("SpringPipelineExecutor registryByConfig [{}], config:{}", ret, config);
        }
        logger.info("SpringPipelineExecutor init success.");
        return true;
    }

    /**
     * registry by config
     *
     * @param config
     * @param handlerMap
     * @return
     */
    private boolean registryByConfig(PipelineConfig config, Map<String, PipelineHandler> handlerMap) {
        if (StringTool.isBlank(config.getName())) {
            logger.info("SpringPipelineExecutor registryByConfig[{}] fail. name is null.", config);
            return false;
        }
        if (CollectionTool.isEmpty(config.getHandlerList())) {
            logger.info("SpringPipelineExecutor registryByConfig[{}] fail. handlerList is null.");
        }

        // pipeline
        Pipeline pipeline = new Pipeline()
                .name(config.getName())
                .status(PipelineStatus.RUNTIME.getStatus());

        // add handler
        for (String handlerName : config.getHandlerList()) {
            PipelineHandler handler = handlerMap.get(handlerName);
            if (handler == null) {
                logger.info("SpringPipelineExecutor registryByConfig[{}] fail. handlerName[{}] not exists.", config, handlerName);
                return false;
            }
            pipeline.addLast(handler);
        }

        // registry
        return this.registry(pipeline);
    }

    @Override
    public void destroy() throws Exception {
        logger.info("SpringPipelineExecutor destroy.");
    }

}

