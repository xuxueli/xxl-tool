package com.xxl.tool.emoji.transformer;

import com.xxl.tool.emoji.fitzpatrick.FitzpatrickAction;
import com.xxl.tool.emoji.model.UnicodeCandidate;

/**
 * Emoji 转换器
 *
 * <p>对文本中识别出的每个 Unicode Emoji 调用一次，返回用于替换该 Emoji 的文本。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public interface EmojiTransformer {

    /**
     * 转换单个 Unicode Emoji
     *
     * @param unicodeCandidate    待转换的 Emoji 候选，包含 Emoji、可选肤色修饰符及位置，不可为 {@code null}
     * @param fitzpatrickAction   肤色修饰符处理策略，可为 {@code null}（由实现决定默认行为）
     * @return                    替换文本；返回空字符串表示删除该 Emoji，不应返回 {@code null}
     */
    public String transform(UnicodeCandidate unicodeCandidate, FitzpatrickAction fitzpatrickAction);

}