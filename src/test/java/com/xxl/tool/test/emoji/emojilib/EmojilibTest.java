package com.xxl.tool.test.emoji.emojilib;

import com.google.gson.Gson;
import com.xxl.tool.emoji.model.Emoji;
import com.xxl.tool.test.emoji.data.EmojiDataTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * https://github.com/muan/emojilib
 */
public class EmojilibTest {


    public static void main(String[] args) throws IOException {
        List<Emoji> emojiList = parseEmojiList();
    }


    private static List<Emoji> parseEmojiList() throws IOException  {
        InputStream stream = EmojiDataTest.class.getResourceAsStream("/xxl-tool/emoji/emojilib/emojis.json");
        String emojiJson = inputStreamToString(stream);

        Gson gson = new Gson();
        Map<String, Map<String, Object>> emojiArr = gson.fromJson(emojiJson, Map.class);


        List<Emoji> emojiList = new LinkedList<>();
        for (Map.Entry<String, Map<String, Object>> emojiItem: emojiArr.entrySet()) {

            List<String> aliases = Arrays.asList(emojiItem.getKey());
            String emoji = emojiItem.getValue().get("char")+"";
            String unicode = emoji;
            boolean supports_fitzpatrick = Boolean.valueOf(String.valueOf(emojiItem.getValue().get("fitzpatrick_scale")));
            List<String> tags = (List<String>) emojiItem.getValue().get("keywords");

            emojiList.add(new Emoji(unicode, aliases, tags, supports_fitzpatrick));
        }
        return emojiList;
    }

    private static String inputStreamToString(InputStream stream) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader isr = new InputStreamReader(stream, "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        String read;
        while ((read = br.readLine()) != null) {
            sb.append(read);
        }
        br.close();
        return sb.toString();
    }


}
