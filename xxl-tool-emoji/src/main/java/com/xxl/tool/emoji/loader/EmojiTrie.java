package com.xxl.tool.emoji.loader;

import com.xxl.tool.emoji.model.Emoji;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * emoji trie
 *
 * 字典树：哈希树的变种，用于统计和排序大量的字符串，最大限度地减少无谓的字符串比较，查询效率比哈希表高。
 * Trie树的插入、删除、查找的操作都是一样的，只需要简单的对树进行一遍遍历即可，时间复杂度：O（n）（n是字符串的长度）。
 *
 * @author xuxueli 2018-07-06 20:15:22
 */
public class EmojiTrie {

    private Node root = new Node();

    /**
     * construct trie tree
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
     * @param emojis
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
     * check if contain (full or partially) an emoji.
     * <p>
     *
     *      Matches.EXACTLY     :   if char sequence in its entirety is an emoji
     *      Matches.POSSIBLY    :   if char sequence matches prefix of an emoji
     *      Matches.IMPOSSIBLE  :   if char sequence matches no emoji or prefix of an
     *
     * @param sequence
     * @return
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
     * find emoji by unicode
     *
     * @param unicode
     * @return
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

    public enum Matches {
        EXACTLY, POSSIBLY, IMPOSSIBLE;

        public boolean exactMatch() {
            return this == EXACTLY;
        }

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
