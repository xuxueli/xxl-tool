package com.xxl.tool.pipeline;

import com.xxl.tool.core.StringTool;

/**
 * pipeline status
 *
 * @author xuxueli 2024-01-01
 */
public enum PipelineStatus {

    RUNTIME("RUNTIME", "运行中"),
    PAUSED("PAUSED", "暂停"),
    FAILED("FAILED", "异常");

    private final String status;
    private final String desc;

    PipelineStatus(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }

    /**
     * find by pipeline status
     *
     * @param status
     * @return
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
