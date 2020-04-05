package com.xxl.tool.emoji.model;

import com.xxl.tool.emoji.fitzpatrick.Fitzpatrick;

/**
 * unicode candidate
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class UnicodeCandidate {
    private final Emoji emoji;
    private final Fitzpatrick fitzpatrick;
    private final int startIndex;

    public UnicodeCandidate(Emoji emoji, String fitzpatrick, int startIndex) {
        this.emoji = emoji;
        this.fitzpatrick = Fitzpatrick.fitzpatrickFromUnicode(fitzpatrick);
        this.startIndex = startIndex;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public boolean hasFitzpatrick() {
        return getFitzpatrick() != null;
    }

    public Fitzpatrick getFitzpatrick() {
        return fitzpatrick;
    }

    public String getFitzpatrickType() {
        return hasFitzpatrick() ? fitzpatrick.name().toLowerCase() : "";
    }

    public String getFitzpatrickUnicode() {
        return hasFitzpatrick() ? fitzpatrick.unicode : "";
    }

    public int getEmojiStartIndex() {
        return startIndex;
    }

    public int getEmojiEndIndex() {
        return startIndex + emoji.getUnicode().length();
    }

    public int getFitzpatrickEndIndex() {
        return getEmojiEndIndex() + (fitzpatrick != null ? 2 : 0);
    }

}