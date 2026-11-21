package com.xxl.tool.emoji.loader;

import com.xxl.tool.emoji.model.Emoji;

import java.util.List;

/**
 * Emoji 数据加载器
 *
 * <p>定义 Emoji 数据来源的扩展点；默认实现为 {@code LocalEmojiDataLoader}，
 * 可通过 {@link com.xxl.tool.emoji.factory.EmojiFactory#setEmojiLoader(EmojiDataLoader)} 替换。</p>
 *
 * <p>实现类在加载失败时可返回 {@code null} 或空集合，由调用方决定如何处理。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public abstract class EmojiDataLoader {

    /**
     * 加载全部 Emoji 数据
     *
     * @return Emoji 列表；加载失败或无数据时返回 {@code null} 或空集合
     */
    public abstract List<Emoji> loadEmojiData();

}
