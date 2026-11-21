package com.xxl.tool.emoji.loader;

import com.xxl.tool.emoji.model.Emoji;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Emoji Unicode 字典树（Trie）
 *
 * <p>字典树：哈希树的变种，用于统计和排序大量的字符串，最大限度地减少无谓的字符串比较，
 * 查询效率比哈希表高。Trie 树的插入、删除、查找都只需对树进行一次遍历，
 * 时间复杂度为 O(n)（n 为字符串长度）。</p>
 *
 * <p>用于在文本中按最长匹配识别 Emoji Unicode。实例构建完成后不再修改，可并发读取。</p>
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiTrie {

    private Node root = new Node();

    /**
     * 构建 Emoji 字典树
     *
     * <p>
     *      Emoji-A：abc
     *      Emoji-B：abd
     *      Emoji-C：abef
     * <p>
     *      root -- a -- b -- c (Emoji-A)
     *                     -- d (Emoji-B)
     *                     -- e -- f (Emoji-C)
     *
     * @param emojis   全部 Emoji，不可为 {@code null}；Emoji 的 Unicode 不可为 {@code null}
     */
    public EmojiTrie(Collection<Emoji> emojis) {
        for (Emoji emoji : emojis) {
            Node tree = root;
            for (char c : emoji.getUnicode().toCharArray()) {
                if (!tree.hasChild(c)) {
                    tree.addChild(c);
                }
                tree = tree.getChild(c);
            }
            tree.setEmoji(emoji);
        }
    }

    /**
     * 判断字符序列与 Emoji 的匹配程度（完整匹配或作为某个 Emoji 的前缀）
     *
     * <p>
     *
     *      Matches.EXACTLY     :   整个字符序列是一个 Emoji
     *      Matches.POSSIBLY    :   字符序列是某个 Emoji 的前缀
     *      Matches.IMPOSSIBLE  :   字符序列不匹配任何 Emoji 或其前缀
     *
     * @param sequence   待判断字符序列，为 {@code null} 时按 {@link Matches#POSSIBLY} 返回
     * @return           匹配程度，不会返回 {@code null}
     */
    public Matches isEmoji(char[] sequence) {
        if (sequence == null) {
            return Matches.POSSIBLY;
        }

        Node tree = root;
        for (char c : sequence) {
            if (!tree.hasChild(c)) {
                return Matches.IMPOSSIBLE;
            }
            tree = tree.getChild(c);
        }

        return tree.isEndOfEmoji() ? Matches.EXACTLY : Matches.POSSIBLY;
    }

    /**
     * 按 Unicode 精确查找 Emoji
     *
     * @param unicode   Emoji Unicode，不可为 {@code null}
     * @return          完全匹配的 Emoji；未匹配（或只是某个 Emoji 的前缀）时返回 {@code null}
     */
    public Emoji getEmoji(String unicode) {
        Node tree = root;
        for (char c : unicode.toCharArray()) {
            if (!tree.hasChild(c)) {
                return null;
            }
            tree = tree.getChild(c);
        }
        return tree.getEmoji();
    }

    /**
     * 字符序列与 Emoji 的匹配结果：
     * <ul>
     *   <li>{@link #EXACTLY}：完整匹配一个 Emoji；</li>
     *   <li>{@link #POSSIBLY}：匹配某个 Emoji 的前缀；</li>
     *   <li>{@link #IMPOSSIBLE}：无法匹配任何 Emoji 或其前缀。</li>
     * </ul>
     */
    public enum Matches {
        EXACTLY, POSSIBLY, IMPOSSIBLE;

        /**
         * 是否为完整匹配
         *
         * @return 完整匹配返回 {@code true}
         */
        public boolean exactMatch() {
            return this == EXACTLY;
        }

        /**
         * 是否完全无法匹配
         *
         * @return 无法匹配任何 Emoji 或其前缀时返回 {@code true}
         */
        public boolean impossibleMatch() {
            return this == IMPOSSIBLE;
        }
    }

    private class Node {
        private Map<Character, Node> children = new HashMap<Character, Node>();
        private Emoji emoji;

        private void setEmoji(Emoji emoji) {
            this.emoji = emoji;
        }

        private Emoji getEmoji() {
            return emoji;
        }

        private boolean hasChild(char child) {
            return children.containsKey(child);
        }

        private void addChild(char child) {
            children.put(child, new Node());
        }

        private Node getChild(char child) {
            return children.get(child);
        }

        private boolean isEndOfEmoji() {
            return emoji != null;
        }
    }

}
