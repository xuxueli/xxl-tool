package com.xxl.tool.pipeline;

import com.xxl.tool.core.StringTool;

/**
 * Pipeline 状态
 *
 * <p>标识 Pipeline 是否可被执行；{@link PipelineExecutor} 只执行 {@link #RUNTIME} 状态的
 * Pipeline。枚举不可变，线程安全。</p>
 *
 * @author xuxueli 2024-01-01
 */
public enum PipelineStatus {

    /**
     * 运行中，可被执行
     */
    RUNTIME("RUNTIME", "运行中"),
    /**
     * 已暂停，不执行
     */
    PAUSED("PAUSED", "暂停"),
    /**
     * 异常状态，不执行
     */
    FAILED("FAILED", "异常");

    private final String status;
    private final String desc;

    PipelineStatus(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    /**
     * 获取状态标识
     *
     * @return 状态标识
     */
    public String getStatus() {
        return status;
    }

    /**
     * 获取状态描述
     *
     * @return 中文描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 按状态标识查找枚举
     *
     * <p>区分大小写；空白或未匹配时返回 {@code null}。</p>
     *
     * @param status   状态标识，可为 {@code null}
     * @return         匹配的状态；未匹配或参数为空白时返回 {@code null}
     */
    public static PipelineStatus findByStatus(String status) {
        if (StringTool.isBlank(status)) {
            return null;
        }
        for (PipelineStatus pipelineStatus : PipelineStatus.values()) {
            if (pipelineStatus.getStatus().equals(status)) {
                return pipelineStatus;
            }
        }
        return null;
    }

}
