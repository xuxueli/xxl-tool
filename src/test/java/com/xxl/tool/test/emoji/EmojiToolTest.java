package com.xxl.tool.test.emoji;

import com.xxl.tool.emoji.EmojiTool;
import com.xxl.tool.emoji.encode.EmojiEncode;

/**
 * emoji tool test
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiToolTest {

    public static void main(String[] args) {

        String input = "一朵美丽的茉莉🌹";
        System.out.println("unicode：" + input);

        // 1、alias
        String aliases = EmojiTool.encodeUnicode(input, EmojiEncode.ALIASES);
        System.out.println("\naliases encode: " + aliases);
        System.out.println("aliases decode: " + EmojiTool.decodeToUnicode(aliases, EmojiEncode.ALIASES));

        // 2、html decimal
        String decimal = EmojiTool.encodeUnicode(input, EmojiEncode.HTML_DECIMAL);
        System.out.println("\ndecimal encode: " + decimal);
        System.out.println("decimal decode: " + EmojiTool.decodeToUnicode(decimal, EmojiEncode.HTML_DECIMAL));

        // 3、html hex decimal
        String hexdecimal = EmojiTool.encodeUnicode(input, EmojiEncode.HTML_HEX_DECIMAL);
        System.out.println("\nhexdecimal encode: " + hexdecimal);
        System.out.println("hexdecimal decode: " + EmojiTool.decodeToUnicode(hexdecimal, EmojiEncode.HTML_HEX_DECIMAL));

    }

}
