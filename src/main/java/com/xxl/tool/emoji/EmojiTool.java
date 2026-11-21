package com.xxl.tool.emoji;

import com.xxl.tool.emoji.encode.EmojiEncode;
import com.xxl.tool.emoji.factory.EmojiFactory;
import com.xxl.tool.emoji.fitzpatrick.FitzpatrickAction;
import com.xxl.tool.emoji.model.AliasCandidate;
import com.xxl.tool.emoji.model.Emoji;
import com.xxl.tool.emoji.model.UnicodeCandidate;
import com.xxl.tool.emoji.transformer.EmojiTransformer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Emoji 工具
 *
 * <p>提供 Unicode Emoji 与别名（{@code :alias:}）、HTML 实体（十进制/十六进制）之间的相互转换，
 * 以及 Emoji 查找、删除和判断能力。</p>
 *
 * <p>别名与 HTML 编码互转依赖 {@link EmojiFactory} 中维护的 Emoji 数据，该数据在
 * {@link EmojiFactory} 类初始化时通过 {@code EmojiDataLoader} 一次性加载；
 * 加载完成后相关静态数据只读，因此本类方法可并发调用。</p>
 *
 * <p>输入为 {@code null} 时，除 {@link #isEmoji(String)} 外均会抛出 {@link NullPointerException}。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiTool {


    // ------------------------ [encode unicode emoji] ------------------------

    /**
     * 按转换器处理输入串中的全部 Unicode Emoji
     * [unicode emoji >> other]
     *
     * @param input               待处理字符串，不可为 {@code null}
     * @param transformer         Emoji 转换器，对每个 Unicode Emoji 调用一次，不可为 {@code null}
     * @param fitzpatrickAction   肤色修饰符处理策略，可为 {@code null}（由转换器自行决定默认行为）
     * @return                    转换后的字符串
     */
    private static String encodeUnicode(String input, EmojiTransformer transformer, FitzpatrickAction fitzpatrickAction) {
        int prev = 0;
        StringBuilder sb = new StringBuilder();
        List<UnicodeCandidate> replacements = EmojiFactory.getUnicodeCandidates(input);
        for (UnicodeCandidate candidate : replacements) {
            sb.append(input.substring(prev, candidate.getEmojiStartIndex()));

            sb.append(transformer.transform(candidate, fitzpatrickAction));
            prev = candidate.getFitzpatrickEndIndex();
        }

        return sb.append(input.substring(prev)).toString();
    }

    /**
     * 将输入串中的 Unicode Emoji 编码为指定形式
     *
     * <p>{@code emojiEncode} 为 {@code null} 时按 {@link EmojiEncode#ALIASES} 处理，
     * {@code fitzpatrickAction} 为 {@code null} 时按 {@link FitzpatrickAction#PARSE} 处理。</p>
     *
     * @param input               待编码字符串，不可为 {@code null}
     * @param emojiEncode         目标编码类型，可为 {@code null}，默认 {@link EmojiEncode#ALIASES}
     * @param fitzpatrickAction   肤色修饰符处理策略，可为 {@code null}，默认 {@link FitzpatrickAction#PARSE}
     * @return                    编码后的字符串
     */
    public static String encodeUnicode(String input, EmojiEncode emojiEncode, FitzpatrickAction fitzpatrickAction) {
        if (emojiEncode == null) {
            emojiEncode = EmojiEncode.ALIASES;
        }
        if (fitzpatrickAction == null) {
            fitzpatrickAction = FitzpatrickAction.PARSE;
        }
        return encodeUnicode(input, emojiEncode.getEmojiTransformer(), fitzpatrickAction);
    }

    /**
     * 将输入串中的 Unicode Emoji 编码为指定形式，肤色修饰符按 {@link FitzpatrickAction#PARSE} 处理
     *
     * @param input               待编码字符串，不可为 {@code null}
     * @param emojiEncode         目标编码类型，为 {@code null} 时按 {@link EmojiEncode#ALIASES} 处理
     * @return                    编码后的字符串
     */
    public static String encodeUnicode(String input, EmojiEncode emojiEncode) {
        return encodeUnicode(input, emojiEncode, FitzpatrickAction.PARSE);
    }

    /**
     * 将输入串中的 Unicode Emoji 编码为别名，肤色修饰符按 {@link FitzpatrickAction#PARSE} 处理
     *
     * @param input   待编码字符串，不可为 {@code null}
     * @return        编码后的字符串
     */
    public static String encodeUnicode(String input) {
        return encodeUnicode(input, EmojiEncode.ALIASES);
    }


    // ------------------------ [decode to unicode emoji] ------------------------

    /**
     * 将别名、HTML 实体还原为 Unicode Emoji（含肤色修饰符）
     *
     * <p>[unicode emoji &lt;&lt; alias | html decimal | html hex]</p>
     *
     * <p>{@code emojiEncode} 为 {@code null} 时依次替换别名、HTML 十进制实体和 HTML 十六进制实体；
     * 指定某一类型时仅替换该类型。别名中的肤色后缀会转换为对应的 Unicode 修饰符。</p>
     *
     * @param input         待还原字符串，不可为 {@code null}
     * @param emojiEncode   要还原的编码类型，可为 {@code null}（还原全部类型）
     * @return              还原后的字符串
     */
    public static String decodeToUnicode(String input, EmojiEncode emojiEncode) {

        String result = input;

        if (emojiEncode==null || emojiEncode==EmojiEncode.ALIASES) {
            // replace the aliases by their unicode
            List<AliasCandidate> candidates = EmojiFactory.getAliasCandidates(input);   // get all alias
            for (AliasCandidate candidate : candidates) {
                Emoji emoji = EmojiFactory.getForAlias(candidate.alias);
                if (emoji != null) {
                    if (emoji.supportsFitzpatrick() || (!emoji.supportsFitzpatrick() && candidate.fitzpatrick == null)) {
                        String replacement = emoji.getUnicode();
                        if (candidate.fitzpatrick != null) {
                            replacement += candidate.fitzpatrick.unicode;
                        }
                        result = result.replace(":" + candidate.fullString + ":", replacement);
                    }
                }
            }
        }

        for (Emoji emoji : EmojiFactory.getAll()) {
            // replace the html by their unicode
            if (emojiEncode==null || emojiEncode==EmojiEncode.HTML_DECIMAL) {
                result = result.replace(emoji.getHtmlDecimal(), emoji.getUnicode());
            }
            // replace the html-hex by their unicode
            if (emojiEncode==null || emojiEncode==EmojiEncode.HTML_HEX_DECIMAL) {
                result = result.replace(emoji.getHtmlHexadecimal(), emoji.getUnicode());
            }
        }

        return result;
    }

    /**
     * 将别名、HTML 十进制实体和 HTML 十六进制实体全部还原为 Unicode Emoji
     *
     * @param input   待还原字符串，不可为 {@code null}
     * @return        还原后的字符串
     */
    public static String decodeToUnicode(String input) {
        return decodeToUnicode(input, null);
    }


    // ------------------------ [remove unicode emoji] ------------------------

    /**
     * 删除输入串中的 Unicode Emoji
     *
     * <p>[unicode emoji >> remove]</p>
     *
     * <p>默认删除全部 Emoji；{@code emojisToKeep} 中命中的 Emoji 会被保留（含原肤色修饰符），
     * 其优先级高于 {@code emojisToRemove}。由于默认行为即全部删除，{@code emojisToRemove}
     * 实际只用于显式表达“要删除的集合”，对结果不产生额外影响。</p>
     *
     * @param input            待处理字符串，不可为 {@code null}
     * @param emojisToRemove   需要删除的 Emoji 集合，可为 {@code null}（不指定）
     * @param emojisToKeep     需要保留的 Emoji 集合，可为 {@code null}（不保留任何 Emoji）
     * @return                 删除（或保留）后的字符串
     */
    public static String removeEmojis(String input, final Collection<Emoji> emojisToRemove, final Collection<Emoji> emojisToKeep) {
        EmojiTransformer emojiTransformer = new EmojiTransformer() {
            /**
             * 转换单个 Unicode Emoji：命中删除集合或不在保留集合时删除，否则保留原 Emoji 及肤色修饰符
             *
             * @param unicodeCandidate    待转换的 Emoji 候选
             * @param fitzpatrickAction   肤色修饰符处理策略（本实现未使用）
             * @return                    空字符串表示删除，否则为原 Emoji Unicode
             */
            public String transform(UnicodeCandidate unicodeCandidate, FitzpatrickAction fitzpatrickAction) {

                boolean ifDelete = true;

                if (emojisToRemove!=null && emojisToRemove.size()>0 && emojisToRemove.contains(unicodeCandidate.getEmoji())) {
                    ifDelete = true;
                }

                if (emojisToKeep!=null && emojisToKeep.size()>0 && emojisToKeep.contains(unicodeCandidate.getEmoji())) {
                    ifDelete = false;
                }

                if (ifDelete) {
                    return "";
                } else {
                    return unicodeCandidate.getEmoji().getUnicode() + unicodeCandidate.getFitzpatrickUnicode();
                }

            }
        };

        return encodeUnicode(input, emojiTransformer, null);
    }


    // ------------------------ [find unicode emoji] ------------------------

    /**
     * 查找输入串中的全部 Emoji
     *
     * <p>按出现顺序返回，重复出现的 Emoji 会重复返回；返回值只包含 Emoji 基础 Unicode，
     * 不包含肤色修饰符。</p>
     *
     * @param input   待查找字符串，不可为 {@code null}
     * @return        Emoji Unicode 列表，未找到时返回空列表，不会返回 {@code null}
     */
    public static List<String> findEmojis(String input) {
        List<UnicodeCandidate> emojis = EmojiFactory.getUnicodeCandidates(input);
        List<String> result = new ArrayList<String>();
        for (UnicodeCandidate emoji : emojis) {
            result.add(emoji.getEmoji().getUnicode());
        }
        return result;
    }

    /**
     * 判断给定字符串是否为一个 Emoji 的完整 Unicode
     *
     * <p>要求输入与某个 Emoji 的 Unicode 完全一致，仅是前缀或包含肤色修饰符时返回 {@code false}。</p>
     *
     * @param unicode   待判断字符串，可为 {@code null}
     * @return          是完整 Emoji Unicode 返回 {@code true}，否则（含 {@code null}）返回 {@code false}
     */
    public static boolean isEmoji(String unicode) {
        Emoji emoji = EmojiFactory.getByUnicode(unicode);
        return emoji!=null?true:false;
    }

}
