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
 * 本地 Pipeline 配置加载器
 *
 * <p>基于 {@link ConcurrentHashMap} 在内存中保存 Pipeline 配置，可通过 {@link #registry} 注册。
 * 同名配置只保留首次注册的实例（{@code putIfAbsent}），后续注册不会覆盖。
 * 方法可并发调用。</p>
 *
 * @author xuxueli 2024-01-01
 */
public class LocalPipelineConfigLoader implements PipelineConfigLoader {

    // config store
    private final Map<String, PipelineConfig> pipelineConfigMap = new ConcurrentHashMap<>();

    /**
     * 注册配置
     *
     * <p>名称为空白或 Handler 列表为空时不做注册。同名配置已存在时保留原配置，
     * 但仍返回 {@code true}。</p>
     *
     * @param pipelineConfig   待注册配置，不可为 {@code null}
     * @return                 入参校验通过返回 {@code true}；名称为空白或 Handler 列表为空返回 {@code false}
     * @throws NullPointerException {@code pipelineConfig} 为 {@code null} 时抛出
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
     * 按名称加载配置
     *
     * @param name   Pipeline 名称
     * @return       对应配置；不存在时返回 {@code null}
     */
    @Override
    public PipelineConfig load(String name) {
        return pipelineConfigMap.get(name);
    }

    /**
     * 加载全部配置
     *
     * @return 全部配置的快照列表；无配置时返回空列表
     */
    @Override
    public List<PipelineConfig> loadAll() {
        return new ArrayList<>(pipelineConfigMap.values());
    }

}
