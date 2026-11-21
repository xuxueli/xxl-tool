package com.xxl.tool.datastructure;

import java.util.HashMap;
import java.util.Map;

/**
 * 字典树（前缀树）：以字符为边存储字符串集合，支持整词查询与前缀匹配。
 *
 * <p>语义与边界：
 * <ul>
 *   <li>按 UTF-16 {@code char} 逐字符精确匹配，区分大小写，不限于 {@code a-z}，可存储任意字符。</li>
 *   <li>字符串不允许为 {@code null}；允许插入空字符串，插入后 {@link #search(String)} 与
 *       {@link #startsWith(String)} 对空串均返回 {@code true}。</li>
 *   <li>不支持删除。节点使用 {@link HashMap} 稀疏存储子节点，内存占用与所有单词的字符总数成正比。</li>
 * </ul>
 *
 * <p>线程安全：非线程安全，并发读写需由调用方自行同步。
 *
 * @author xuxueli 2025-12-13
 */
public class Trie {

    /**
     * root node
     */
    private final TrieNode root;

    /**
     * 创建空字典树。
     */
    public Trie() {
        root = new TrieNode();
    }

    /**
     * 插入一个单词；已存在时仅保持结束标记，不产生重复节点。
     *
     * @param word 待插入单词，不能为 {@code null}；可为空串
     * @throws NullPointerException {@code word} 为 {@code null} 时抛出
     */
    public void insert(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    /**
     * 查询完整单词是否已插入（必须是某个单词的结尾，仅有相同前缀不算）。
     *
     * @param word 待查询单词，不能为 {@code null}；区分大小写
     * @return 整词存在返回 {@code true}，否则返回 {@code false}
     * @throws NullPointerException {@code word} 为 {@code null} 时抛出
     */
    public boolean search(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return false;
            }
        }
        return current.isEndOfWord;
    }

    /**
     * 判断是否存在以指定字符串为前缀的单词；前缀本身也可以是某个已插入的完整单词。
     *
     * @param prefix 待检查前缀，不能为 {@code null}；区分大小写，空串返回 {@code true}
     * @return 存在该前缀返回 {@code true}，否则返回 {@code false}
     * @throws NullPointerException {@code prefix} 为 {@code null} 时抛出
     */
    public boolean startsWith(String prefix) {
        TrieNode current = root;
        for (char ch : prefix.toCharArray()) {
            current = current.children.get(ch);
            if (current == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * 字典树节点：每个节点保存结束标记与子节点映射。
     *
     * <p>非线程安全，由所属 {@link Trie} 统一管理。
     */
    private static class TrieNode {

        /**
         * 子节点映射
         *
         * <p>key：子节点对应的字符，可为任意 UTF-16 {@code char}（不限于 a-z）
         * <p>value：子节点
         */
        private Map<Character, TrieNode> children;

        /**
         * 是否为某个单词的结尾
         */
        private boolean isEndOfWord;

        /**
         * 创建空节点。
         */
        public TrieNode() {
            this.children = new HashMap<>();
            this.isEndOfWord = false;
        }

        /**
         * 获取子节点映射。
         *
         * @return 子节点映射，非 {@code null}
         */
        public Map<Character, TrieNode> getChildren() {
            return children;
        }

        /**
         * 替换子节点映射。
         *
         * @param children 新的子节点映射
         */
        public void setChildren(Map<Character, TrieNode> children) {
            this.children = children;
        }

        /**
         * 是否为单词结尾。
         *
         * @return 是结尾返回 {@code true}
         */
        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        /**
         * 设置单词结尾标记。
         *
         * @param endOfWord 是否为单词结尾
         */
        public void setEndOfWord(boolean endOfWord) {
            isEndOfWord = endOfWord;
        }

    }


}
