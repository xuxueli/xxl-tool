package com.xxl.tool.pipeline.config;

import java.util.List;

/**
 * pipeline config
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineConfig {

    private String name;
    private List<String> handlerList;

    public PipelineConfig() {
    }
    public PipelineConfig(String name, List<String> handlerList) {
        this.name = name;
        this.handlerList = handlerList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getHandlerList() {
        return handlerList;
    }

    public void setHandlerList(List<String> handlerList) {
        this.handlerList = handlerList;
    }

    @Override
    public String toString() {
        return "PipelineConfig{" +
                "name='" + name + '\'' +
                ", handlerList=" + handlerList +
                '}';
    }

}
