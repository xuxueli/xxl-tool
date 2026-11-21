package com.xxl.tool.emoji.model;


import com.xxl.tool.emoji.exception.XxlEmojiException;
import com.xxl.tool.emoji.fitzpatrick.Fitzpatrick;

import java.util.Collections;
import java.util.List;

/**
 * Emoji 模型
 *
 * <p>描述一个 Emoji 的 Unicode、别名、标签、是否支持肤色修饰符，并在构造时生成
 * HTML 十进制/十六进制实体表示。实例不可变（别名与标签列表被包装为不可修改视图），线程安全。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class Emoji {

    /** Emoji 基础 Unicode（不含肤色修饰符），非空 */
    private final String unicode;
    /** 别名列表，如 {@code smile}；构造后被包装为不可修改视图 */
    private final List<String> aliases;
    /** 标签列表，构造后被包装为不可修改视图 */
    private final List<String> tags;
    /** 是否支持肤色修饰符 */
    private final boolean supportsFitzpatrick;

    /** HTML 十进制实体，如 {@code &#128512;} */
    private final String htmlDec;
    /** HTML 十六进制实体，如 {@code &#x1f600;} */
    private final String htmlHex;


    /**
     * 创建 Emoji
     *
     * @param unicode              Emoji 的 Unicode，不可为 {@code null}
     * @param aliases              别名列表，不可为 {@code null}，构造后不可修改
     * @param tags                 标签列表，不可为 {@code null}，构造后不可修改
     * @param supportsFitzpatrick  是否支持肤色修饰符
     */
    public Emoji(String unicode,
                 List<String> aliases,
                 List<String> tags,
                 boolean supportsFitzpatrick) {

        // fill data
        this.unicode = unicode;
        this.aliases = Collections.unmodifiableList(aliases);   // refuse modefy
        this.tags = Collections.unmodifiableList(tags);
        this.supportsFitzpatrick = supportsFitzpatrick;

        // make dec/hex data
        int stringLength = getUnicode().length();
        String[] pointCodes = new String[stringLength];
        String[] pointCodesHex = new String[stringLength];

        int count = 0;
        for (int offset = 0; offset < stringLength; ) {
            final int codePoint = getUnicode().codePointAt(offset);

            pointCodes[count] = String.format("&#%d;", codePoint);
            pointCodesHex[count++] = String.format("&#x%x;", codePoint);

            offset += Character.charCount(codePoint);
        }
        this.htmlDec = stringJoin(pointCodes, count);
        this.htmlHex = stringJoin(pointCodesHex, count);

    }

    /**
     * string join, like "String.join" in java8
     */
    private String stringJoin(String[] array, int count) {
        String joined = "";
        for (int i = 0; i < count; i++)
            joined += array[i];
        return joined;
    }


    // ---------------------- get ----------------------

    public boolean supportsFitzpatrick() {
        return this.supportsFitzpatrick;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    public List<String> getTags() {
        return this.tags;
    }

    public String getUnicode() {
        return this.unicode;
    }

    public String getHtmlDecimal() {
        return this.htmlDec;
    }

    public String getHtmlHexadecimal() {
        return this.htmlHex;
    }


    // ---------------------- other ----------------------

    /**
     * 获取带肤色修饰符的 Unicode
     *
     * @param fitzpatrick   肤色等级，可为 {@code null}（此时返回基础 Unicode）
     * @return              Emoji Unicode（可能追加肤色修饰符）
     * @throws XxlEmojiException    当前 Emoji 不支持肤色修饰符时抛出
     */
    public String getUnicode(Fitzpatrick fitzpatrick) {
        if (!this.supportsFitzpatrick()) {
            throw new XxlEmojiException("cannot get unicode with given fitzpatrick modifier, the emoji doesn't support fitzpatrick.");
        } else if (fitzpatrick == null) {
            return this.getUnicode();
        }
        return this.getUnicode() + fitzpatrick.unicode;
    }

    // effect when equals
    /**
     * 判断是否相等，仅比较基础 Unicode，忽略别名、标签与肤色能力
     *
     * @param other   待比较对象
     * @return        同为 {@link Emoji} 且 Unicode 相同返回 {@code true}
     */
    @Override
    public boolean equals(Object other) {
        return !(other == null || !(other instanceof Emoji)) &&
                ((Emoji) other).getUnicode().equals(getUnicode());
    }

    /**
     * 计算哈希值，基于基础 Unicode
     *
     * @return 哈希值
     */
    @Override
    public int hashCode() {
        return unicode.hashCode();
    }

}
