package com.xxl.tool.emoji.loader.impl;

import com.xxl.tool.emoji.loader.EmojiDataLoader;
import com.xxl.tool.emoji.model.Emoji;
import com.xxl.tool.io.FileTool;
import com.xxl.tool.json.GsonTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * emoji data loader
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class LocalEmojiDataLoader extends EmojiDataLoader {
    private static final Logger logger = LoggerFactory.getLogger(LocalEmojiDataLoader.class);

    private static final String PATH = "/xxl-tool/emoji/xxl-tool-emoji.json";

    public List<Emoji> loadEmojiData()  {

        try {
            // json
            String emojiJson = FileTool.readString(LocalEmojiDataLoader.class.getResource(PATH).getPath());

            // emoji data
            List<Object> emojiArr = GsonTool.fromJson(emojiJson, List.class);
            if (emojiArr==null || emojiArr.isEmpty()) {
                return null;
            }

            // parse dto
            List<Emoji> emojis = new ArrayList<>();
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

            return emojis;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

}
