package com.xxl.tool.emoji.factory;

import com.xxl.tool.emoji.exception.XxlEmojiException;
import com.xxl.tool.emoji.loader.EmojiDataLoader;
import com.xxl.tool.emoji.loader.EmojiTrie;
import com.xxl.tool.emoji.loader.impl.LocalEmojiDataLoader;
import com.xxl.tool.emoji.model.AliasCandidate;
import com.xxl.tool.emoji.model.Emoji;
import com.xxl.tool.emoji.model.UnicodeCandidate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Emoji 工厂
 *
 * <p>负责加载并索引 Emoji 数据，提供按别名、标签、Unicode 查询 Emoji，以及从文本中
 * 解析别名候选（{@link AliasCandidate}）和 Unicode 候选（{@link UnicodeCandidate}）的能力。</p>
 *
 * <p>数据在类初始化时由 {@link #emojiLoader} 一次性加载到静态索引中；如需替换数据源，
 * 应在首次使用本类之前调用 {@link #setEmojiLoader(EmojiDataLoader)}，或替换后显式调用
 * {@link #loadEmoji()} 重新加载。加载完成后索引只读，查询方法可并发调用；
 * 重新加载过程本身未做同步，不应与查询并发执行。</p>
 *
 * <p>Emoji 数据依赖 Gson 解析（见 {@code LocalEmojiDataLoader}），Gson 为 provided 依赖。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiFactory {

    private static List<Emoji> ALL_EMOJIS = null;                   // 全部 emoji
    private static EmojiTrie EMOJI_TRIE = null;                     // unicode 字典树

    private static Map<String, Emoji> EMOJIS_BY_ALIAS = null;       // alias -> emoji，多对一
    private static Map<String, Set<Emoji>> EMOJIS_BY_TAG = null;    // tag -> emoji 集合，多对多

    private static EmojiDataLoader emojiLoader = new LocalEmojiDataLoader();

    /**
     * 设置 Emoji 数据加载器
     *
     * <p>仅替换后续加载使用的加载器，不会立即重新加载；需要在类初始化前设置，
     * 或在设置后调用 {@link #loadEmoji()} 使新数据源生效。</p>
     *
     * @param emojiLoader   Emoji 数据加载器，不可为 {@code null}
     */
    public static void setEmojiLoader(EmojiDataLoader emojiLoader) {
        EmojiFactory.emojiLoader = emojiLoader;
    }

    /**
     * 加载 Emoji 数据并重建别名、标签和 Unicode 索引
     *
     * <p>加载结果为空或 {@code null} 时抛出 {@link XxlEmojiException}。
     * 该方法非线程安全，应在类初始化阶段或单线程场景调用。</p>
     *
     * @throws XxlEmojiException    加载器返回空数据时抛出
     */
    public static void loadEmoji(){
        List<Emoji> emojis = emojiLoader.loadEmojiData();
        if (emojis==null || emojis.size()==0) {
            throw new XxlEmojiException("emoji loader fail");
        }

        ALL_EMOJIS = emojis;
        EMOJI_TRIE = new EmojiTrie(ALL_EMOJIS);

        EMOJIS_BY_ALIAS = new HashMap<String, Emoji>();
        EMOJIS_BY_TAG = new HashMap<String, Set<Emoji>>();

        for (Emoji emoji : ALL_EMOJIS) {
            for (String alias : emoji.getAliases()) {
                EMOJIS_BY_ALIAS.put(alias, emoji);
            }
            for (String tag : emoji.getTags()) {
                if (EMOJIS_BY_TAG.get(tag) == null) {
                    EMOJIS_BY_TAG.put(tag, new HashSet<Emoji>());
                }
                EMOJIS_BY_TAG.get(tag).add(emoji);
            }
        }

    }

    static {
        loadEmoji();
    }


    // ---------------------- get unicode/alias emoji util ----------------------

    /**
     * 按别名查询 Emoji
     *
     * <p>别名两侧的 {@code :} 会被忽略；匹配不到时返回 {@code null}。</p>
     *
     * @param alias    Emoji 别名，可带或不带两侧冒号，可为 {@code null}
     * @return         匹配的 Emoji，未找到或参数为 {@code null} 时返回 {@code null}
     */
    public static Emoji getForAlias(String alias) {
        if (alias == null) {
            return null;
        }
        return EMOJIS_BY_ALIAS.get(trimAlias(alias));
    }

    private static String trimAlias(String alias) {
        String result = alias;
        if (result.startsWith(":")) {
            result = result.substring(1, result.length());
        }
        if (result.endsWith(":")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    /**
     * 按标签查询 Emoji 集合
     *
     * @param tag   标签，可为 {@code null}
     * @return      该标签下的 Emoji 集合（内部集合引用，请勿修改）；未找到或参数为 {@code null} 时返回 {@code null}
     */
    public static Set<Emoji> getForTag(String tag) {
        if (tag == null) {
            return null;
        }
        return EMOJIS_BY_TAG.get(tag);
    }

    /**
     * 获取全部 Emoji 标签
     *
     * @return 标签集合（内部集合引用，请勿修改），无数据时为空集合
     */
    public static Set<String> getAllTags() {
        return EMOJIS_BY_TAG.keySet();
    }

    /**
     * 按完整 Unicode 查询 Emoji
     *
     * <p>仅精确匹配 Emoji 基础 Unicode，不支持带肤色修饰符的完整序列。</p>
     *
     * @param unicode   Emoji Unicode，可为 {@code null}
     * @return          匹配的 Emoji，未找到或参数为 {@code null} 时返回 {@code null}
     */
    public static Emoji getByUnicode(String unicode) {
        if (unicode == null) {
            return null;
        }
        return EMOJI_TRIE.getEmoji(unicode);
    }

    /**
     * 获取全部 Emoji
     *
     * @return 全部 Emoji 列表（内部列表引用，请勿修改），未加载时可能为 {@code null}
     */
    public static List<Emoji> getAll() {
        return ALL_EMOJIS;
    }


    // ------------------------ find unicode/alias emoji util ------------------------

    private static final Pattern ALIAS_CANDIDATE_PATTERN = Pattern.compile("(?<=:)\\+?(\\w|\\||\\-)+(?=:)");

    /**
     * 从文本中解析全部别名候选
     *
     * <p>识别形如 {@code :alias:}、{@code :+alias:}、{@code :alias|type_x:} 的片段，
     * 按出现顺序返回。别名部分作为 {@code alias}，竖线后的肤色类型交由
     * {@link AliasCandidate} 解析。</p>
     *
     * @param input   待解析文本，不可为 {@code null}
     * @return        别名候选列表，未匹配时返回空列表，不会返回 {@code null}
     */
    public static List<AliasCandidate> getAliasCandidates(String input) {
        List<AliasCandidate> candidates = new ArrayList<AliasCandidate>();

        Matcher matcher = ALIAS_CANDIDATE_PATTERN.matcher(input);
        matcher = matcher.useTransparentBounds(true);
        while (matcher.find()) {
            String match = matcher.group();
            if (!match.contains("|")) {
                candidates.add(new AliasCandidate(match, match, null));
            } else {
                String[] splitted = match.split("\\|");
                if (splitted.length == 2 || splitted.length > 2) {
                    candidates.add(new AliasCandidate(match, splitted[0], splitted[1]));
                } else {
                    candidates.add(new AliasCandidate(match, match, null));
                }
            }
        }
        return candidates;
    }

    /**
     * 从文本中解析全部 Unicode Emoji 候选
     *
     * <p>按出现顺序返回，若 Emoji 后紧跟肤色修饰符则一并关联；每个候选的起止索引只覆盖
     * Emoji 本身（不含肤色修饰符）。</p>
     *
     * @param input   待解析文本，不可为 {@code null}
     * @return        Unicode 候选列表，未匹配时返回空列表，不会返回 {@code null}
     */
    public static List<UnicodeCandidate> getUnicodeCandidates(String input) {
        char[] inputCharArray = input.toCharArray();
        List<UnicodeCandidate> candidates = new ArrayList<>();
        UnicodeCandidate next;
        for (int i = 0; (next = getNextUnicodeCandidate(inputCharArray, i)) != null; i = next.getFitzpatrickEndIndex()) {
            candidates.add(next);
        }

        return candidates;
    }

    /**
     * 查找 {@code start} 之后的下一个 Unicode Emoji 候选
     *
     * @param chars   待查找字符数组
     * @param start   起始下标（含）
     * @return        下一个候选；未找到时返回 {@code null}
     */
    protected static UnicodeCandidate getNextUnicodeCandidate(char[] chars, int start) {
        for (int i = start; i < chars.length; i++) {
            int emojiEnd = getFirstEmojiEndPos(chars, i);

            if (emojiEnd != -1) {
                Emoji emoji = EmojiFactory.getByUnicode(new String(chars, i, emojiEnd - i));
                String fitzpatrickString = (emojiEnd + 2 <= chars.length) ? new String(chars, emojiEnd, 2) : null;
                return new UnicodeCandidate(emoji, fitzpatrickString, i);
            }
        }

        return null;
    }

    /**
     * 查找从 {@code startPos} 开始的最长 Emoji 结束下标
     *
     * <p>逐个扩展字符并查询字典树，始终保留最近一次精确匹配的位置，从而在 Emoji 互为前缀时
     * 匹配最长的一个。</p>
     *
     * @param text       待查找字符数组
     * @param startPos   起始下标（含）
     * @return           Emoji 结束下标（不含）；未匹配时返回 {@code -1}
     */
    protected static int getFirstEmojiEndPos(char[] text, int startPos) {
        int best = -1;
        for (int j = startPos + 1; j <= text.length; j++) {
            EmojiTrie.Matches status = EMOJI_TRIE.isEmoji(Arrays.copyOfRange(text, startPos, j));

            if (status.exactMatch()) {
                best = j;
            } else if (status.impossibleMatch()) {
                return best;
            }
        }

        return best;
    }

}
