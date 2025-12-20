package com.xxl.tool.test.emoji.emojilib;

import com.xxl.tool.emoji.model.Emoji;
import com.xxl.tool.io.FileTool;
import com.xxl.tool.json.GsonTool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * https://github.com/muan/emojilib
 */
public class EmojilibTest {
    private static final Logger logger = LoggerFactory.getLogger(EmojilibTest.class);


    @Test
    public void parseEmojiList() throws IOException  {
        String emojiJson = FileTool.readString(EmojilibTest.class.getResource("/xxl-tool/emoji/emojilib/emojis.json").getPath());
        Map<String, Map<String, Object>> emojiArr = GsonTool.fromJson(emojiJson, Map.class);

        List<Emoji> emojiList = new LinkedList<>();
        for (Map.Entry<String, Map<String, Object>> emojiItem: emojiArr.entrySet()) {

            List<String> aliases = Arrays.asList(emojiItem.getKey());
            String emoji = emojiItem.getValue().get("char")+"";
            String unicode = emoji;
            boolean supports_fitzpatrick = Boolean.valueOf(String.valueOf(emojiItem.getValue().get("fitzpatrick_scale")));
            List<String> tags = (List<String>) emojiItem.getValue().get("keywords");

            emojiList.add(new Emoji(unicode, aliases, tags, supports_fitzpatrick));
        }
        logger.info("emojiList:{}", emojiList.size());
    }

}
