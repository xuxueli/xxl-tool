package com.xxl.tool.emoji.loader;

import com.xxl.tool.emoji.model.Emoji;

import java.util.List;

/**
 * emoji loader
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public abstract class EmojiDataLoader {

    public abstract List<Emoji> loadEmojiData();

}
