package com.xxl.tool.pipeline.config;

import java.util.List;

/**
 * Pipeline 配置加载器
 *
 * <p>Pipeline 配置的来源扩展点，用于按名称加载单个配置或加载全部配置。
 * 默认实现 {@code LocalPipelineConfigLoader} 基于本地 Map；使用方可实现本接口接入
 * 配置中心、数据库等外部存储。</p>
 *
 * <p>具体实现需说明自身线程安全语义。</p>
 *
 * @author xuxueli 2024-01-01
 */
public interface PipelineConfigLoader {

    /**
     * 按名称加载配置
     *
     * @param name   Pipeline 名称
     * @return       对应配置；不存在时返回 {@code null}（由实现约定）
     */
    public PipelineConfig load(String name);

    /**
     * 加载全部配置
     *
     * @return 全部配置；无配置时返回空列表（由实现约定），不应返回 {@code null}
     */
    public List<PipelineConfig> loadAll();

}
