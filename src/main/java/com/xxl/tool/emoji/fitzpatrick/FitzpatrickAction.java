package com.xxl.tool.emoji.fitzpatrick;

/**
 * 肤色修饰符处理策略
 *
 * <p>用于 Emoji 编码时控制 Emoji 后紧跟的 Fitzpatrick 肤色修饰符如何被处理。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public enum FitzpatrickAction {

    /**
     * 解析并保留肤色信息：编码为别名时输出 {@code :alias|type_x:}
     */
    PARSE,

    /**
     * 删除肤色修饰符
     */
    REMOVE,

    /**
     * 忽略肤色处理，保留原始修饰符 Unicode
     */
    IGNORE
}