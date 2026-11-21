package com.xxl.tool.emoji.fitzpatrick;

/**
 * Fitzpatrick 肤色等级
 *
 * <p>参考 <a href="https://en.wikipedia.org/wiki/Fitzpatrick_scale">Fitzpatrick scale</a>，
 * 用于描述 Unicode Emoji 的肤色修饰符（skin tone modifier）。</p>
 *
 * <p>枚举不可变，线程安全。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public enum Fitzpatrick {

    /**
     * [🏻]["\uD83C\uDFFB"][]
     *
     * Type I (scores 0–6) always burns, never tans (pale white; blond or red hair; blue eyes; freckles).
     * Type II (scores 7–13) usually burns, tans minimally (white; fair; blond or red hair; blue, green, or hazel eyes)
     */
    TYPE_1_2("🏻"),

    /**
     * [🏼]["\uD83C\uDFFC"]
     *
     * Type III (scores 14–20) sometimes mild burn, tans uniformly (cream white; fair with any hair or eye color)
     */
    TYPE_3("🏼"),

    /**
     * [🏽]["\uD83C\uDFFD"]
     *
     * Type IV (scores 21–27) burns minimally, always tans well (moderate brown)
     */
    TYPE_4("🏽"),

    /**
     * [🏾]["\uD83C\uDFFE"]
     *
     * Type V (scores 28–34) very rarely burns, tans very easily (dark brown)
     */
    TYPE_5("🏾"),

    /**
     * [🏿]["\uD83C\uDFFF"]
     *
     * Fitzpatrick modifier of type 6 (black)
     *
     * Type VI (scores 35–36) Never burns, never tans (deeply pigmented dark brown to darkest brown)
     */
    TYPE_6("🏿");

    /**
     * 该等级对应的 Unicode 肤色修饰符
     */
    public final String unicode;

    Fitzpatrick(String unicode) {
        this.unicode = unicode;
    }


    /**
     * 按 Unicode 肤色修饰符查找等级
     *
     * @param unicode   肤色修饰符，可为 {@code null}
     * @return          匹配的等级，未匹配或参数为 {@code null} 时返回 {@code null}
     */
    public static Fitzpatrick fitzpatrickFromUnicode(String unicode) {
        for (Fitzpatrick item : values()) {
            if (item.unicode.equals(unicode)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 按等级名称查找等级，不区分大小写
     *
     * @param type   等级名称，如 {@code type_1_2}、{@code TYPE_3}，不可为 {@code null}
     * @return       匹配的等级，名称非法时返回 {@code null}
     * @throws NullPointerException    {@code type} 为 {@code null} 时抛出
     */
    public static Fitzpatrick fitzpatrickFromType(String type) {
        try {
            return Fitzpatrick.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
