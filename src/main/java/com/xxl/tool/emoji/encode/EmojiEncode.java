package com.xxl.tool.emoji.encode;


import com.xxl.tool.emoji.fitzpatrick.FitzpatrickAction;
import com.xxl.tool.emoji.model.UnicodeCandidate;
import com.xxl.tool.emoji.transformer.EmojiTransformer;

/**
 * Emoji 编码类型
 *
 * <p>每种类型内置一个 {@link EmojiTransformer}，决定把 Unicode Emoji 转换成哪种文本形式。
 * 转换时按 {@link FitzpatrickAction} 处理肤色修饰符，传入 {@code null} 时按
 * {@link FitzpatrickAction#PARSE} 处理。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public enum EmojiEncode {

    /**
     * 编码为别名
     *
     * <p>使用 Emoji 的第一个别名（两侧带 {@code :}）替换 Unicode Emoji。
     * 含肤色修饰符时，PARSE 输出 {@code :alias|type_x:}，IGNORE 保留修饰符 Unicode，
     * REMOVE 丢弃修饰符。</p>
     *
     * <p>[unicode emoji >> first alias]</p>
     *
     */
    ALIASES(new EmojiTransformer() {
        /**
         * 将 Unicode Emoji 转为第一个别名
         *
         * @param unicodeCandidate    待转换的 Emoji 候选
         * @param fitzpatrickAction   肤色修饰符处理策略，{@code null} 视为 PARSE
         * @return                    别名文本
         */
        public String transform(UnicodeCandidate unicodeCandidate, FitzpatrickAction fitzpatrickAction) {
            if (fitzpatrickAction == null) {
                fitzpatrickAction = FitzpatrickAction.PARSE;
            }

            if (fitzpatrickAction==FitzpatrickAction.PARSE && unicodeCandidate.hasFitzpatrick()) {
                return ":" +
                        unicodeCandidate.getEmoji().getAliases().get(0) +
                        "|" +
                        unicodeCandidate.getFitzpatrickType() +
                        ":";
            } else if (fitzpatrickAction == FitzpatrickAction.IGNORE) {
                return ":" +
                        unicodeCandidate.getEmoji().getAliases().get(0) +
                        ":" +
                        unicodeCandidate.getFitzpatrickUnicode();
            } else {    // REMOVE, default
                return ":" +
                        unicodeCandidate.getEmoji().getAliases().get(0) +
                        ":";
            }

        }
    }),

    /**
     * 编码为 HTML 十进制实体
     *
     * <p>使用 Emoji 的 HTML 十进制表示替换 Unicode Emoji。</p>
     *
     * <p>[unicode emoji >> html decimal]</p>
     *
     */
    HTML_DECIMAL(new EmojiTransformer() {
        /**
         * 将 Unicode Emoji 转为 HTML 十进制实体
         *
         * @param unicodeCandidate    待转换的 Emoji 候选
         * @param fitzpatrickAction   肤色修饰符处理策略，{@code null} 视为 PARSE
         * @return                    HTML 十进制实体文本
         */
        @Override
        public String transform(UnicodeCandidate unicodeCandidate, FitzpatrickAction fitzpatrickAction) {
            if (fitzpatrickAction == null) {
                fitzpatrickAction = FitzpatrickAction.PARSE;
            }

            if (fitzpatrickAction == FitzpatrickAction.IGNORE) {
                return unicodeCandidate.getEmoji().getHtmlDecimal() + unicodeCandidate.getFitzpatrickUnicode();     // IGNORE, will ignored and remain modifier
            } else {    // REMOVE, PARSE, default >> remove
                return unicodeCandidate.getEmoji().getHtmlDecimal();    // parse+remove, will deletec modifier
            }
        }
    }),

    /**
     * 编码为 HTML 十六进制实体
     *
     * <p>使用 Emoji 的 HTML 十六进制表示替换 Unicode Emoji。</p>
     *
     * <p>[unicode emoji >> html hex]</p>
     */
    HTML_HEX_DECIMAL(new EmojiTransformer() {
        /**
         * 将 Unicode Emoji 转为 HTML 十六进制实体
         *
         * @param unicodeCandidate    待转换的 Emoji 候选
         * @param fitzpatrickAction   肤色修饰符处理策略，{@code null} 视为 PARSE
         * @return                    HTML 十六进制实体文本
         */
        @Override
        public String transform(UnicodeCandidate unicodeCandidate, FitzpatrickAction fitzpatrickAction) {
            if (fitzpatrickAction == null) {
                fitzpatrickAction = FitzpatrickAction.PARSE;
            }

            if (fitzpatrickAction == FitzpatrickAction.IGNORE) {
                return unicodeCandidate.getEmoji().getHtmlHexadecimal() + unicodeCandidate.getFitzpatrickUnicode();
            } else {    // REMOVE, PARSE, default >> remove
                return unicodeCandidate.getEmoji().getHtmlHexadecimal();
            }
        }
    });

    private EmojiTransformer emojiTransformer;

    EmojiEncode(EmojiTransformer emojiTransformer) {
        this.emojiTransformer = emojiTransformer;
    }
    /**
     * 获取当前编码类型对应的转换器
     *
     * @return Emoji 转换器，不会返回 {@code null}
     */
    public EmojiTransformer getEmojiTransformer() {
        return emojiTransformer;
    }
}
