package com.xxl.tool.datastructure;

import java.util.HashMap;
import java.util.Map;

/**
 * trie tree
 *
 * @author xuxueli 2025-12-13
 */
public class Trie {

    /**
     * root node
     */
    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }

    /**
     * insert word
     *
     * @param word the word to be inserted
     */
    public void insert(String word) {
        TrieNode current = root;
        for (char ch : word.toCharArray()) {
            current = current.children.computeIfAbsent(ch, c -> new TrieNode());
        }
        current.isEndOfWord = true;
    }

    /**
     * search wolrd
     *
     * @param word the word to be searched
     * @return true if the word is found, exactly exist
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
     * judge whether the specified prefix exists
     *
     * @param prefix the prefix to be checked
     * @return true if the specified prefix exists, may be a prefix of another word
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
     * trie node
     */
    private static class TrieNode {

        /**
         * children
         *
         *  key: the child node`s character, range [a-z]
         *  value: the child node
         */
        private Map<Character, TrieNode> children;

        /**
         * is end of word
         */
        private boolean isEndOfWord;

        public TrieNode() {
            this.children = new HashMap<>();
            this.isEndOfWord = false;
        }

        public Map<Character, TrieNode> getChildren() {
            return children;
        }

        public void setChildren(Map<Character, TrieNode> children) {
            this.children = children;
        }

        public boolean isEndOfWord() {
            return isEndOfWord;
        }

        public void setEndOfWord(boolean endOfWord) {
            isEndOfWord = endOfWord;
        }

    }


}
