package com.xxl.tool.emoji.encode;


import com.xxl.tool.emoji.fitzpatrick.FitzpatrickAction;
import com.xxl.tool.emoji.model.UnicodeCandidate;
import com.xxl.tool.emoji.transformer.EmojiTransformer;

/**
 * emoji encode type
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public enum EmojiEncode {

    /**
     * encode unicode to aliases
     *
     * replace emoji unicode by one of their first alias (between 2 ':')
     *
     * [unicode emoji >> first alias ]
     *
     */
    ALIASES(new EmojiTransformer() {
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
     * encode unicode html decimal
     *
     * replace unicode emoji by their html representation.
     *
     * [unicode emoji >> html hex ]
     *
     */
    HTML_DECIMAL(new EmojiTransformer() {
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
     * encode unicode html hex decimal
     *
     * replace unicode emoji by their html hex representation
     *
     * [unicode emoji >> html hex ]
     */
    HTML_HEX_DECIMAL(new EmojiTransformer() {
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
    public EmojiTransformer getEmojiTransformer() {
        return emojiTransformer;
    }
}
