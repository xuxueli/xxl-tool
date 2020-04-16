package com.xxl.tool.emoji.loader.impl;

import com.google.gson.Gson;
import com.xxl.tool.emoji.loader.EmojiDataLoader;
import com.xxl.tool.emoji.model.Emoji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * emoji data loader
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class LocalEmojiDataLoader extends EmojiDataLoader {

    private static final String PATH = "/xxl-tool/emoji/xxl-tool-emoji.json";

    public List<Emoji> loadEmojiData()  {

        InputStream stream = null;
        try {
            // json
            stream = LocalEmojiDataLoader.class.getResourceAsStream(PATH);
            String emojiJson = inputStreamToString(stream);

            // emoji data
            Gson gson = new Gson();
            List<Object> emojiArr = gson.fromJson(emojiJson, List.class);
            if (emojiArr==null || emojiArr.size()==0) {
                return null;
            }

            // parse dto
            List<Emoji> emojis = new ArrayList<Emoji>();
            for (Object emojiItem: emojiArr) {
                if (emojiItem instanceof Map){

                    Map<String, Object> emojiItemMap = (Map<String, Object>) emojiItem;

                    String unicode = String.valueOf(emojiItemMap.get("unicode"));

                    List<String> aliases = null;
                    if (emojiItemMap.containsKey("aliases") && emojiItemMap.get("aliases") instanceof List) {
                        aliases = (List<String>) emojiItemMap.get("aliases");
                    }
                    List<String> tags = null;
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

            return emojis;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream!=null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
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
