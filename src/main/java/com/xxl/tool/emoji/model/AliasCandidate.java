package com.xxl.tool.emoji.model;


import com.xxl.tool.emoji.fitzpatrick.Fitzpatrick;

/**
 * 别名候选
 *
 * <p>表示从文本中解析出的一个 Emoji 别名片段，如 {@code :smile:} 或
 * {@code :smile|type_3:}。不可变，线程安全。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class AliasCandidate {
    /**
     * 完整匹配文本，包含别名及可选肤色后缀（不含两侧冒号），如 {@code smile|type_3}
     */
    public final String fullString;
    /**
     * 别名本身，不含肤色后缀
     */
    public final String alias;
    /**
     * 肤色等级；未指定肤色或肤色名称非法时为 {@code null}
     */
    public final Fitzpatrick fitzpatrick;

    /**
     * 创建别名候选
     *
     * @param fullString          完整匹配文本，不含两侧冒号
     * @param alias               别名
     * @param fitzpatrickString   肤色等级名称，可为 {@code null}
     */
    public AliasCandidate(String fullString, String alias, String fitzpatrickString) {
        this.fullString = fullString;
        this.alias = alias;
        if (fitzpatrickString == null) {
            this.fitzpatrick = null;
        } else {
            this.fitzpatrick = Fitzpatrick.fitzpatrickFromType(fitzpatrickString);
        }
    }

}