package com.xxl.tool.pipeline.config;

import java.util.List;

/**
 * pipeline config loader
 *
 * @author xuxueli 2024-01-01
 */
public interface PipelineConfigLoader {

    /**
     * load config
     *
     * @param name
     * @return
     */
    public PipelineConfig load(String name);

    /**
     * load all configs
     *
     * @return
     */
    public List<PipelineConfig> loadAll();

}
