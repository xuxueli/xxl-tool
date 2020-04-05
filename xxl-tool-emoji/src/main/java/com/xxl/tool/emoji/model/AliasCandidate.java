package com.xxl.tool.emoji.model;


import com.xxl.tool.emoji.fitzpatrick.Fitzpatrick;

/**
 * alias candidate
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class AliasCandidate {
    public final String fullString;
    public final String alias;
    public final Fitzpatrick fitzpatrick;

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