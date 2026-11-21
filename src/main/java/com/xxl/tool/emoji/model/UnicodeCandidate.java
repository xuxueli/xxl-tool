package com.xxl.tool.emoji.model;

import com.xxl.tool.emoji.fitzpatrick.Fitzpatrick;

/**
 * Unicode Emoji 候选
 *
 * <p>表示文本中某位置识别出的一个 Unicode Emoji，可选关联其后的肤色修饰符。
 * 索引均基于 Java 字符（UTF-16 code unit）下标。实例不可变，线程安全。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class UnicodeCandidate {
    /** 识别到的 Emoji，非空 */
    private final Emoji emoji;
    /** 紧随其后的肤色修饰符；无有效修饰符时为 {@code null} */
    private final Fitzpatrick fitzpatrick;
    /** Emoji 在文本中的起始下标（UTF-16 code unit，含） */
    private final int startIndex;

    /**
     * 创建 Unicode 候选
     *
     * @param emoji          识别到的 Emoji，不可为 {@code null}
     * @param fitzpatrick    紧随其后的肤色修饰符 Unicode，可为 {@code null} 或非肤色字符
     * @param startIndex     Emoji 在文本中的起始下标
     */
    public UnicodeCandidate(Emoji emoji, String fitzpatrick, int startIndex) {
        this.emoji = emoji;
        this.fitzpatrick = Fitzpatrick.fitzpatrickFromUnicode(fitzpatrick);
        this.startIndex = startIndex;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    /**
     * 是否带有肤色修饰符
     *
     * @return 存在有效肤色修饰符返回 {@code true}
     */
    public boolean hasFitzpatrick() {
        return getFitzpatrick() != null;
    }

    public Fitzpatrick getFitzpatrick() {
        return fitzpatrick;
    }

    /**
     * 获取肤色等级名称（小写）
     *
     * @return 如 {@code type_3}，无修饰符时返回空字符串
     */
    public String getFitzpatrickType() {
        return hasFitzpatrick() ? fitzpatrick.name().toLowerCase() : "";
    }

    /**
     * 获取肤色修饰符 Unicode
     *
     * @return 肤色修饰符，无修饰符时返回空字符串
     */
    public String getFitzpatrickUnicode() {
        return hasFitzpatrick() ? fitzpatrick.unicode : "";
    }

    public int getEmojiStartIndex() {
        return startIndex;
    }

    /**
     * 获取 Emoji 结束下标（不含肤色修饰符）
     *
     * @return 结束下标（不含）
     */
    public int getEmojiEndIndex() {
        return startIndex + emoji.getUnicode().length();
    }

    /**
     * 获取肤色修饰符结束下标
     *
     * <p>肤色修饰符固定占 2 个 char；无修饰符时等于 {@link #getEmojiEndIndex()}。</p>
     *
     * @return 结束下标（不含）
     */
    public int getFitzpatrickEndIndex() {
        return getEmojiEndIndex() + (fitzpatrick != null ? 2 : 0);
    }

}