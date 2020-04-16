package com.xxl.tool.test.emoji.fitzpatrick;


import com.xxl.tool.emoji.fitzpatrick.Fitzpatrick;

public class FitzpatrickTest {

    public static void main(String[] args) {
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("\uD83C\uDFFB"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("\uD83C\uDFFC"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("\uD83C\uDFFD"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("\uD83C\uDFFE"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("\uD83C\uDFFF"));

        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("🏻"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("🏼"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("🏽"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("🏾"));
        System.out.println(Fitzpatrick.fitzpatrickFromUnicode("🏿"));
    }

}
