package com.xxl.tool.test.io;

import com.xxl.tool.io.FileTool;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

public class FileToolTest {
    private static final Logger logger = LoggerFactory.getLogger(FileToolTest.class);

    /*@TempDir
    static Path tempDir;*/

    private static Path tempDir;
    private static String testDirPath;
    private static String testFilePath;

    @BeforeAll
    static void setUp() {
        tempDir = Paths.get("/Users/admin/Downloads/file");
        testDirPath = tempDir.resolve("testDir").toString();
        testFilePath = tempDir.resolve("testFile.txt").toString();
    }

    @BeforeEach
    void beforeEach() {
        // 清理环境
        FileTool.delete(testDirPath);
        FileTool.delete(testFilePath);
    }

    // ---------------------- create file 测试 ----------------------

    @Test
    @DisplayName("测试创建文件对象")
    void testFile() {
        File file1 = FileTool.file("test.txt");
        assertNotNull(file1);
        assertEquals("test.txt", file1.getName());

        File file2 = FileTool.file("parent", "child");
        assertNotNull(file2);
        assertEquals("child", file2.getName());
    }

    @Test
    @DisplayName("测试创建实际文件")
    void testCreateFile() throws IOException {
        File file = FileTool.createFile(testFilePath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
    }

    @Test
    @DisplayName("测试创建带父路径的实际文件")
    void testCreateFileWithParent() throws IOException {
        String filePath = Paths.get(testDirPath, "subdir", "test.txt").toString();
        File file = FileTool.createFile(filePath);
        assertTrue(file.exists());
        assertTrue(file.isFile());
    }

    // ---------------------- create directories 测试 ----------------------

    @Test
    @DisplayName("测试创建目录")
    void testCreateDirectories() throws IOException {
        boolean result = FileTool.createDirectories(new File(testDirPath));
        assertTrue(result);
        assertTrue(FileTool.exists(testDirPath));
        assertTrue(FileTool.isDirectory(testDirPath));
    }

    @Test
    @DisplayName("测试创建父目录")
    void testCreateParentDirectories() throws IOException {
        String filePath = Paths.get(testDirPath, "subdir", "test.txt").toString();
        boolean result = FileTool.createParentDirectories(new File(filePath));
        assertTrue(result);
        assertTrue(FileTool.exists(testDirPath));
        assertTrue(FileTool.isDirectory(testDirPath));
    }

    // ---------------------- valid base 测试 ----------------------

    @Test
    @DisplayName("测试判断目录")
    void testIsDirectory() throws IOException {
        FileTool.createDirectories(new File(testDirPath));
        assertTrue(FileTool.isDirectory(testDirPath));
        assertFalse(FileTool.isDirectory(testFilePath));
    }

    @Test
    @DisplayName("测试判断文件")
    void testIsFile() throws IOException {
        FileTool.createFile(testFilePath);
        assertTrue(FileTool.isFile(testFilePath));
        assertFalse(FileTool.isFile(testDirPath));
    }

    @Test
    @DisplayName("测试判断空文件/目录")
    void testIsEmpty() throws IOException {
        // 空文件
        FileTool.createFile(testFilePath);
        assertTrue(FileTool.isEmpty(new File(testFilePath)));

        // 空目录
        FileTool.createDirectories(new File(testDirPath));
        assertTrue(FileTool.isEmpty(new File(testDirPath)));

        // 非空文件
        FileTool.writeString(testFilePath, "test content");
        assertFalse(FileTool.isEmpty(new File(testFilePath)));
    }

    @Test
    @DisplayName("测试判断是否为同一文件")
    void testIsSameFile() throws IOException {
        File file1 = FileTool.createFile(testFilePath);
        File file2 = new File(testFilePath);
        assertTrue(FileTool.isSameFile(file1, file2));
    }

    // ---------------------- valid exists 测试 ----------------------

    @Test
    @DisplayName("测试文件是否存在")
    void testExists() throws IOException {
        assertFalse(FileTool.exists(testFilePath));
        FileTool.createFile(testFilePath);
        assertTrue(FileTool.exists(testFilePath));
    }

    // ---------------------- info size 测试 ----------------------

    @Test
    @DisplayName("测试获取文件大小")
    void testSize() throws IOException {
        FileTool.createFile(testFilePath);
        assertEquals(0, FileTool.size(testFilePath));

        FileTool.writeString(testFilePath, "test");
        assertEquals(4, FileTool.size(testFilePath));
    }

    // ---------------------- info line 测试 ----------------------

    @Test
    @DisplayName("测试获取文件行数")
    void testTotalLines() throws IOException {
        FileTool.createFile(testFilePath);
        FileTool.writeString(testFilePath, "line1\nline2\nline3");
        assertEquals(3, FileTool.totalLines(testFilePath));
    }

    // ---------------------- delete + clean 测试 ----------------------

    @Test
    @DisplayName("测试删除文件")
    void testDelete() throws IOException {
        FileTool.createFile(testFilePath);
        assertTrue(FileTool.exists(testFilePath));
        assertTrue(FileTool.delete(testFilePath));
        assertFalse(FileTool.exists(testFilePath));
    }

    @Test
    @DisplayName("测试清空目录")
    void testClean() throws IOException {
        FileTool.createDirectories(new File(testDirPath));
        String filePath = Paths.get(testDirPath, "test.txt").toString();
        FileTool.createFile(filePath);
        assertTrue(FileTool.exists(filePath));

        assertTrue(FileTool.clean(testDirPath));
        assertFalse(FileTool.exists(filePath));
        assertTrue(FileTool.exists(testDirPath));
    }

    // ---------------------- copy 测试 ----------------------

    @Test
    @DisplayName("测试复制文件")
    void testCopy() throws IOException {
        FileTool.createFile(testFilePath);
        FileTool.writeString(testFilePath, "test content");

        String destPath = tempDir.resolve("copiedFile.txt").toString();
        File destFile = FileTool.copy(testFilePath, destPath, false);

        assertTrue(destFile.exists());
        assertEquals("test content", FileTool.readString(destPath));
    }

    // ---------------------- move 测试 ----------------------

    @Test
    @DisplayName("测试移动文件")
    void testMove() throws IOException {
        FileTool.createFile(testFilePath);
        FileTool.writeString(testFilePath, "test content");

        String destPath = tempDir.resolve("movedFile.txt").toString();
        File destFile = FileTool.move(testFilePath, destPath, false);

        assertFalse(FileTool.exists(testFilePath));
        assertTrue(destFile.exists());
        assertEquals("test content", FileTool.readString(destPath));
    }

    // ---------------------- rename 测试 ----------------------

    @Test
    @DisplayName("测试重命名文件")
    void testRename() throws IOException {
        FileTool.createFile(testFilePath);
        FileTool.writeString(testFilePath, "test content");

        File newFile = FileTool.rename(new File(testFilePath), "renamedFile.txt", false);

        assertFalse(FileTool.exists(testFilePath));
        assertTrue(newFile.exists());
        assertEquals("test content", FileTool.readString(newFile.getPath()));
    }

    // ---------------------- write data 测试 ----------------------

    @Test
    @DisplayName("测试写入字符串")
    void testWriteString() throws IOException {
        FileTool.writeString(testFilePath, "test content");
        assertTrue(FileTool.exists(testFilePath));
        assertEquals("test content", FileTool.readString(testFilePath));

        // 测试追加
        FileTool.writeString(testFilePath, " append", true);
        assertEquals("test content append", FileTool.readString(testFilePath));
    }

    // ---------------------- write line 测试 ----------------------

    @Test
    @DisplayName("测试写入多行数据")
    void testWriteLines() throws IOException {
        List<String> lines = Arrays.asList("line1", "line2", "line3");
        FileTool.writeLines(testFilePath, lines);
        assertTrue(FileTool.exists(testFilePath));

        List<String> readLines = FileTool.readLines(testFilePath);
        assertEquals(lines, readLines);
    }

    @Test
    @DisplayName("测试使用Supplier写入多行数据")
    void testWriteLinesWithSupplier() throws IOException {
        List<String> data = Arrays.asList("line1", "line2", "line3", null);
        AtomicInteger index = new AtomicInteger(0);

        Supplier<String> supplier = () -> {
            int i = index.getAndIncrement();
            return i < data.size() ? data.get(i) : null;
        };

        FileTool.writeLines(testFilePath, supplier);
        assertTrue(FileTool.exists(testFilePath));

        List<String> readLines = FileTool.readLines(testFilePath);
        assertEquals(Arrays.asList("line1", "line2", "line3"), readLines);
    }

    // ---------------------- read String 测试 ----------------------

    @Test
    @DisplayName("测试读取字符串")
    void testReadString() throws IOException {
        FileTool.createFile(testFilePath);
        FileTool.writeString(testFilePath, "test content");

        String content = FileTool.readString(testFilePath);
        assertEquals("test content", content);
    }

    // ---------------------- read Lines 测试 ----------------------

    @Test
    @DisplayName("测试读取多行数据")
    void testReadLines() throws IOException {
        List<String> lines = Arrays.asList("line1", "line2", "line3");
        FileTool.writeLines(testFilePath, lines);

        List<String> readLines = FileTool.readLines(testFilePath);
        assertEquals(lines, readLines);
    }

    @Test
    @DisplayName("测试使用Consumer读取多行数据")
    void testReadLinesWithConsumer() throws IOException {
        List<String> lines = Arrays.asList("line1", "line2", "line3");
        FileTool.writeLines(testFilePath, lines);

        List<String> readLines = new ArrayList<>();
        FileTool.readLines(testFilePath, readLines::add);

        assertEquals(lines, readLines);
    }


}
