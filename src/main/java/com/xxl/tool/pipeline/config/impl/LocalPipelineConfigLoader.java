package com.xxl.tool.pipeline.config.impl;

import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.pipeline.config.PipelineConfig;
import com.xxl.tool.pipeline.config.PipelineConfigLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * pipeline config loader, local version
 *
 * @author xuxueli 2024-01-01
 */
public class LocalPipelineConfigLoader implements PipelineConfigLoader {

    // config store
    private final Map<String, PipelineConfig> pipelineConfigMap = new ConcurrentHashMap<>();

    /**
     * register config
     *
     * @param pipelineConfig
     */
    public boolean registry(PipelineConfig pipelineConfig) {
        // valid
        if (StringTool.isBlank(pipelineConfig.getName())
                || CollectionTool.isEmpty(pipelineConfig.getHandlerList())) {
            return false;
        }

        // registry
        pipelineConfigMap.putIfAbsent(pipelineConfig.getName(), pipelineConfig);
        return true;
    }

    /**
     * load config
     *
     * @param name
     * @return
     */
    @Override
    public PipelineConfig load(String name) {
        return pipelineConfigMap.get(name);
    }

    /**
     * load all configs
     *
     * @return
     */
    @Override
    public List<PipelineConfig> loadAll() {
        return new ArrayList<>(pipelineConfigMap.values());
    }

}
