package com.xxl.tool.pipeline.config;

import java.util.List;

/**
 * Pipeline 配置
 *
 * <p>描述一个 Pipeline 的名称，以及按执行顺序排列的 Handler 名称列表。
 * 由 {@link PipelineConfigLoader} 提供，供 Spring 集成按名称查找 Handler Bean 并装配 Pipeline。</p>
 *
 * <p>普通数据对象，非线程安全。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class PipelineConfig {

    /**
     * Pipeline 名称
     */
    private String name;
    /**
     * Handler 名称列表，顺序即执行顺序
     */
    private List<String> handlerList;

    /**
     * 创建空配置（供反序列化使用）
     */
    public PipelineConfig() {
    }

    /**
     * 创建配置
     *
     * @param name          Pipeline 名称
     * @param handlerList   Handler 名称列表，顺序即执行顺序
     */
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
