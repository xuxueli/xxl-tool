//package com.xxl.tool.test.core;
//
//import com.xxl.tool.core.FileTool1;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.Arrays;
//
//public class FileTool1Test {
//    private static Logger logger = LoggerFactory.getLogger(FileTool1Test.class);
//
//    @Test
//    public void test() throws IOException {
//        FileTool1.copyFile("/Users/admin/Downloads/test.txt", "/Users/admin/Downloads/test02.txt");
//    }
//
//    @Test
//    public void test2() throws IOException {
//        FileTool1.copyFileData("/Users/admin/Downloads/test.txt", "/Users/admin/Downloads/test02.txt");
//    }
//
//    @Test
//    public void test3() throws IOException {
//        FileTool1.writeLines("/Users/admin/Downloads/test.txt", Arrays.asList("1", "2", "3"));
//
//        FileTool1.writeLines("/Users/admin/Downloads/test.txt", Arrays.asList("444", "555"), true);
//        FileTool1.writeLines("/Users/admin/Downloads/test.txt", Arrays.asList("666", "777"), true);
//    }
//
//    @Test
//    public void test4() throws IOException {
//        FileTool1.writeString("/Users/admin/Downloads/test.txt", "333333 " + System.lineSeparator());
//
//        FileTool1.writeString("/Users/admin/Downloads/test.txt", "aaaaaaaa", true);
//        FileTool1.writeString("/Users/admin/Downloads/test.txt", "bbbbbbbb", true);
//
//    }
//
//}
