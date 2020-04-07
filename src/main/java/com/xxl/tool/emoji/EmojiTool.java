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
 * emoji tool
 *
 * A flexible emoji library for Java.
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiTool {


    // ------------------------ [encode unicode emoji] ------------------------

    /**
     * detects all unicode emojis, and replaces them with the return value of transformer.transform()
     * [unicode emoji >> other]
     *
     * @param input
     * @param transformer   emoji transformer to apply to each emoji
     * @return              input string with all emojis transformed
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
     * encode emoji unicode
     *
     * @param input
     * @param emojiEncode
     * @param fitzpatrickAction
     * @return
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
     * encode emoji unicode
     *
     * @param input
     * @param emojiEncode
     * @return
     */
    public static String encodeUnicode(String input, EmojiEncode emojiEncode) {
        return encodeUnicode(input, emojiEncode, FitzpatrickAction.PARSE);
    }

    /**
     * encode emoji unicode
     *
     * @param input
     * @return
     */
    public static String encodeUnicode(String input) {
        return encodeUnicode(input, EmojiEncode.ALIASES);
    }


    // ------------------------ [decode to unicode emoji] ------------------------

    /**
     * replace aliases and html representations by their unicode(modifiers).
     *
     * [unicode emoji << alias | html hex ]
     *
     * @param input
     * @param emojiEncode
     * @return
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
     * decode emoji unicode
     *
     * @param input
     * @return
     */
    public static String decodeToUnicode(String input) {
        return decodeToUnicode(input, null);
    }


    // ------------------------ [remove unicode emoji] ------------------------

    /**
     * remove emojis
     *
     * [unicode emoji >> remove ]
     *
     * @param input             default remove all
     * @param emojisToRemove    remove
     * @param emojisToKeep      not remove
     * @return
     */
    public static String removeEmojis(String input, final Collection<Emoji> emojisToRemove, final Collection<Emoji> emojisToKeep) {
        EmojiTransformer emojiTransformer = new EmojiTransformer() {
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
     * find emojis
     *
     * @param input
     * @return
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
     * is emoji
     *
     * @param unicode
     * @return
     */
    public static boolean isEmoji(String unicode) {
        Emoji emoji = EmojiFactory.getByUnicode(unicode);
        return emoji!=null?true:false;
    }

}
