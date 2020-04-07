package com.xxl.tool.emoji.transformer;

import com.xxl.tool.emoji.fitzpatrick.FitzpatrickAction;
import com.xxl.tool.emoji.model.UnicodeCandidate;

/**
 * emoji transformer
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public interface EmojiTransformer {

    /**
     * @param unicodeCandidate
     * @param fitzpatrickAction     the action to apply for the fitzpatrick modifiers
     * @return
     */
    public String transform(UnicodeCandidate unicodeCandidate, FitzpatrickAction fitzpatrickAction);

}