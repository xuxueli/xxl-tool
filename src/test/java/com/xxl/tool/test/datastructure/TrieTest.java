package com.xxl.tool.test.datastructure;

import com.xxl.tool.datastructure.Trie;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrieTest {
    private static final Logger logger = LoggerFactory.getLogger(TrieTest.class);

    @Test
    public void test() {
        Trie trie = new Trie();

        // 插入单词
        trie.insert("apple");
        trie.insert("app");
        trie.insert("application");
        trie.insert("apply");

        // 查询完整单词
        logger.info("Search 'app': " + trie.search("app"));         // true
        logger.info("Search 'apple': " + trie.search("apple"));     // true
        logger.info("Search 'appl': " + trie.search("appl"));       // false

        // 前缀匹配检查
        logger.info("Starts with 'app': " + trie.startsWith("app"));   // true
        logger.info("Starts with 'appli': " + trie.startsWith("appli")); // true
        logger.info("Starts with 'ban': " + trie.startsWith("ban"));   // false
    }
}
