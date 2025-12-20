package com.xxl.tool.test.emoji;

import com.xxl.tool.emoji.EmojiTool;
import com.xxl.tool.emoji.encode.EmojiEncode;
import com.xxl.tool.emoji.loader.impl.LocalEmojiDataLoader;
import com.xxl.tool.emoji.model.Emoji;
import com.xxl.tool.io.FileTool;
import com.xxl.tool.json.GsonTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.*;

/**
 * emoji tool test
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiToolTest {

    @Test
    public void emoji() {

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

    @Test
    public void emoji2() throws IOException {
        List<Emoji> loadEmojiData1 = loadEmojiData("/xxl-tool/emoji/xxl-tool-emoji-origin.json");
        List<Emoji> loadEmojiData = loadEmojiData("/xxl-tool/emoji/xxl-tool-emoji.json");

        Assertions.assertEquals(GsonTool.toJson(loadEmojiData1), GsonTool.toJson(loadEmojiData));
    }

    public List<Emoji> loadEmojiData(String path) throws IOException {

            // json
        String emojiJson = FileTool.readString(LocalEmojiDataLoader.class.getResource(path).getPath());

        // emoji data
        List<Object> emojiArr = GsonTool.fromJson(emojiJson, List.class);
        if (emojiArr==null || emojiArr.isEmpty()) {
            return null;
        }

        // parse dto
        List<Emoji> emojis = new ArrayList<Emoji>();
        for (Object emojiItem: emojiArr) {
            if (emojiItem instanceof Map){

                Map<String, Object> emojiItemMap = (Map<String, Object>) emojiItem;

                String unicode = String.valueOf(emojiItemMap.get("unicode"));

                List<String> aliases = new ArrayList<>();
                if (emojiItemMap.containsKey("aliases") && emojiItemMap.get("aliases") instanceof List) {
                    aliases = (List<String>) emojiItemMap.get("aliases");
                }
                List<String> tags = new ArrayList<>();
                if (emojiItemMap.containsKey("tags") && emojiItemMap.get("tags") instanceof List) {
                    tags = (List<String>) emojiItemMap.get("tags");
                }

                boolean supports_fitzpatrick = false;
                if (emojiItemMap.containsKey("supports_fitzpatrick")) {
                    supports_fitzpatrick = Boolean.valueOf(String.valueOf(emojiItemMap.get("supports_fitzpatrick")));
                }

                Emoji emojiObj = new Emoji(unicode, aliases, tags, supports_fitzpatrick);
                emojis.add(emojiObj);
            }
        }

        Collections.sort(emojis, Comparator.comparing(Emoji::getUnicode));

        return emojis;
    }

}
