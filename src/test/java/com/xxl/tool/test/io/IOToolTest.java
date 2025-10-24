package com.xxl.tool.test.io;

import com.xxl.tool.io.IOTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class IOToolTest {
    private static final Logger logger = LoggerFactory.getLogger(IOToolTest.class);

    @TempDir
    private Path tempDir;

    // Test copy method with normal input and output streams
    @Test
    void testCopyWithNormalStreams() throws IOException {
        // Given: 准备测试数据
        byte[] testData = "Hello World".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = new ByteArrayInputStream(testData);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When: 执行复制操作
        int copiedBytes = IOTool.copy(input, output);

        // Then: 验证结果
        assertEquals(testData.length, copiedBytes);
        assertArrayEquals(testData, output.toByteArray());
    }

    // Test copy method with null input stream
    @Test
    void testCopyWithNullInputStream() {
        // Given: 准备null输入流和有效输出流
        OutputStream output = new ByteArrayOutputStream();

        // When & Then: 验证抛出预期异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            IOTool.copy((InputStream) null, output);
        });

        assertEquals("No InputStream specified", exception.getMessage());
    }

    // Test copy method with null output stream
    @Test
    void testCopyWithNullOutputStream() {
        // Given: 准备有效输入流和null输出流
        InputStream input = new ByteArrayInputStream(new byte[]{1, 2, 3});

        // When & Then: 验证抛出预期异常
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            IOTool.copy(input, null);
        });

        assertEquals("No OutputStream specified", exception.getMessage());
    }

    // Test copy method with byte array
    @Test
    void testCopyWithByteArray() throws IOException {
        // Given: 准备测试数据
        byte[] testData = "Test Data".getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When: 执行复制操作
        IOTool.copy(testData, output);

        // Then: 验证结果
        assertArrayEquals(testData, output.toByteArray());
    }

    // Test close method with valid closeable
    @Test
    void testCloseWithValidCloseable() throws IOException {
        // Given: 准备可关闭资源（使用自定义实现来验证关闭行为）
        final boolean[] closed = {false};
        Closeable closeable = new Closeable() {
            @Override
            public void close() throws IOException {
                closed[0] = true;
            }
        };

        // When: 执行关闭操作
        IOTool.close(closeable);

        // Then: 验证资源被正确关闭
        assertTrue(closed[0]);
    }

    // Test close method with null closeable
    @Test
    void testCloseWithNullCloseable() {
        // When: 执行关闭操作(null资源)
        // Then: 不应抛出异常
        assertDoesNotThrow(() -> IOTool.close(null));
    }

    // Test readBytes method with normal input stream
    @Test
    void testReadBytesWithNormalStream() throws IOException {
        // Given: 准备测试数据
        byte[] testData = "Read Bytes Test".getBytes(StandardCharsets.UTF_8);
        ByteArrayInputStream input = new ByteArrayInputStream(testData);

        // When: 读取字节数据
        byte[] result = IOTool.readBytes(input);

        // Then: 验证结果
        assertArrayEquals(testData, result);
    }

    // Test readBytes method with null input stream
    @Test
    void testReadBytesWithNullStream() throws IOException {
        // When: 读取null流的数据
        byte[] result = IOTool.readBytes(null);

        // Then: 应返回空数组
        assertEquals(0, result.length);
    }

    // Test readString method with normal input stream
    @Test
    void testReadStringWithNormalStream() throws IOException {
        // Given: 准备测试数据
        String testData = "Read String Test";
        ByteArrayInputStream input = new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8));

        // When: 读取字符串数据
        String result = IOTool.readString(input, StandardCharsets.UTF_8);

        // Then: 验证结果
        assertEquals(testData, result);
    }

    // Test readString method with null input stream
    @Test
    void testReadStringWithNullStream() throws IOException {
        // When: 读取null流的字符串
        String result = IOTool.readString(null, StandardCharsets.UTF_8);

        // Then: 应返回空字符串
        assertEquals("", result);
    }

    // Test writeString method with normal data
    @Test
    void testWriteStringWithNormalData() throws IOException {
        // Given: 准备测试数据和输出流
        String testData = "Write String Test";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When: 写入字符串数据
        IOTool.writeString(testData, output);

        // Then: 验证结果
        assertEquals(testData, output.toString(StandardCharsets.UTF_8));
    }

    // Test writeString method with null data
    @Test
    void testWriteStringWithNullData() {
        // Given: 准备null数据和输出流
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When: 写入null数据
        // Then: 不应抛出异常
        assertDoesNotThrow(() -> IOTool.writeString(null, output));
    }

    // Test writeString method with specified charset
    @Test
    void testWriteStringWithSpecifiedCharset() throws IOException {
        // Given: 准备测试数据、输出流和字符集
        String testData = "字符编码测试";
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // When: 使用指定字符集写入数据
        IOTool.writeString(testData, output, StandardCharsets.UTF_8);

        // Then: 验证结果
        assertEquals(testData, output.toString(StandardCharsets.UTF_8));
    }

    // Test newBufferedWriter method with default parameters
    @Test
    void testNewBufferedWriterWithDefaultParameters() throws IOException {
        // Given: 准备文件路径
        File testFile = tempDir.resolve("test.txt").toFile();

        // When: 创建BufferedWriter
        BufferedWriter writer = IOTool.newBufferedWriter(testFile, false);

        // Then: 验证返回了有效的writer
        assertNotNull(writer);
        writer.close();
    }

    // Test newBufferedWriter method with all parameters
    @Test
    void testNewBufferedWriterWithAllParameters() throws IOException {
        // Given: 准备文件路径和其他参数
        File testFile = tempDir.resolve("test2.txt").toFile();

        // When: 使用所有参数创建BufferedWriter
        BufferedWriter writer = IOTool.newBufferedWriter(testFile, true, StandardCharsets.UTF_8, 512);

        // Then: 验证返回了有效的writer
        assertNotNull(writer);
        writer.close();
    }


}
