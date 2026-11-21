package com.xxl.tool.io;

import com.xxl.tool.core.AssertTool;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * IO 工具
 *
 * <p>提供流拷贝、资源关闭、按字符集读写字符串以及构建 {@link BufferedWriter}
 * 等基础能力：
 * <ul>
 *     <li>拷贝方法按 {@link #BUFFER_SIZE}（8KB）缓冲区循环读写，适用于大文件/大流，
 *     不会一次性载入全部内容；</li>
 *     <li>是否关闭输入/输出流由参数显式控制，关闭动作在 {@code finally} 中执行，
 *     读写异常时同样生效；关闭异常被忽略；</li>
 *     <li>字符串读写默认使用 UTF-8，写入时不追加换行符，读取时保留文件原有换行；</li>
 *     <li>无状态工具类，方法可并发调用；已关闭的流不应再使用，由调用方保证。</li>
 * </ul>
 *
 * @author xuxueli 2024-10-27
 */
public class IOTool {

    /**
     * 默认缓冲区大小，单位字节（8KB）；分块拷贝与字符串读写按此大小分配缓冲区
     */
    public static final int BUFFER_SIZE = 1024 * 8;


    // ---------------------- copy ----------------------

    /**
     * 拷贝输入流到输出流，完成后关闭两个流（即使发生异常）。
     *
     * <p>内部按 {@link #BUFFER_SIZE} 分块读写并 flush 输出流；等价于
     * {@code copy(input, output, true, true)}。
     *
     * @param input 输入流，不能为 {@code null}
     * @param output 输出流，不能为 {@code null}
     * @return 实际拷贝的字节数
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws IOException 读取、写入或 flush 失败；异常时仍会尝试关闭两个流
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, true,  true);
    }

    /**
     * 拷贝输入流到输出流，可选择结束后是否关闭流。
     *
     * <p>按 {@link #BUFFER_SIZE} 分块循环读写，结束时 flush 输出流；关闭动作在
     * {@code finally} 中执行，因此读写抛出异常时也会关闭对应流。关闭产生的
     * {@link IOException} 会被忽略（见 {@link #close(Closeable)}）。
     *
     * @param input 输入流，不能为 {@code null}
     * @param output 输出流，不能为 {@code null}
     * @param closeInput {@code true} 结束后关闭输入流；{@code false} 保持打开
     * @param closeOutput {@code true} 结束后关闭输出流；{@code false} 保持打开
     * @return 实际拷贝的字节数
     * @throws IllegalArgumentException 任一流参数为 {@code null}
     * @throws IOException 从输入流读取或向输出流写入/flush 失败
     */
    public static int copy(InputStream input, OutputStream output, boolean closeInput, boolean closeOutput) throws IOException {
        AssertTool.notNull(input, "No InputStream specified");
        AssertTool.notNull(output, "No OutputStream specified");

        try {
            int byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            output.flush();
            return byteCount;
        } finally {
            if (closeInput) {
                close(input);
            }
            if (closeOutput) {
                close(output);
            }
        }
    }

    /**
     * 将字节数组写入输出流，完成后关闭输出流。
     *
     * <p>等价于 {@code copy(input, output, true)}。
     *
     * @param input 待写入的字节数组，不能为 {@code null}
     * @param output 输出流，不能为 {@code null}
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws IOException 写入失败；异常时仍会尝试关闭输出流
     */
    public static void copy(byte[] input, OutputStream output) throws IOException {
        copy(input, output, true);
    }

    /**
     * 将字节数组一次性写入输出流，可选择是否关闭输出流。
     *
     * <p>不做分块，直接调用 {@link OutputStream#write(byte[])}；关闭动作在
     * {@code finally} 中执行，但不主动 flush。
     *
     * @param input 待写入的字节数组，不能为 {@code null}
     * @param output 输出流，不能为 {@code null}
     * @param closeOutput {@code true} 结束后关闭输出流；{@code false} 保持打开
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws IOException 写入失败；异常时仍会尝试关闭输出流
     */
    public static void copy(byte[] input, OutputStream output, boolean closeOutput) throws IOException {
        AssertTool.notNull(input, "No input byte array specified");
        AssertTool.notNull(output, "No OutputStream specified");

        try {
            output.write(input);
        } finally {
            if (closeOutput) {
                close(output);
            }
        }
    }


    // ---------------------- close ----------------------

    /**
     * 关闭 {@link Closeable}，忽略关闭过程中抛出的异常。
     *
     * <p>参数为 {@code null} 时直接返回；常用于 {@code finally} 中兜底关闭，
     * 避免关闭异常覆盖原始异常。
     *
     * @param closeable 待关闭资源，可为 {@code null}
     */
    public static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ex) {
            // ignore
        }
    }


    // ---------------------- read from InputStream ----------------------

    /**
     * 读取输入流的全部字节，结束后关闭输入流。
     *
     * <p>内容一次性读入内存（{@link ByteArrayOutputStream}），大文件请改用
     * {@link #copy(InputStream, OutputStream)} 边读边写。输入流为 {@code null}
     * 时返回长度为 0 的数组。
     *
     * @param inputStream 输入流，可为 {@code null}
     * @return 读取到的字节数组；参数为 {@code null} 时返回空数组
     * @throws IOException 读取失败；异常时仍会关闭输入流
     */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }

        // InputStream 2 byte[]
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(inputStream, out);
        return out.toByteArray();
    }

    /**
     * 按 UTF-8 读取输入流的全部字符，结束后关闭输入流。
     *
     * <p>等价于 {@code readString(inputStream, StandardCharsets.UTF_8)}。
     *
     * @param inputStream 输入流，可为 {@code null}
     * @return 读取到的字符串；参数为 {@code null} 时返回 {@code ""}
     * @throws IOException 读取失败；异常时仍会关闭输入流
     */
    public static String readString(InputStream inputStream) throws IOException {
        return readString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * 按指定字符集读取输入流的全部字符，结束后关闭输入流。
     *
     * <p>使用 {@link InputStreamReader} 一次性解码到内存，文件中的 CR/LF 原样
     * 保留，不转换、不追加换行；输入流为 {@code null} 时返回 {@code ""}。
     * 关闭动作在 {@code finally} 中执行，输入流与 reader 都会被关闭。
     *
     * @param inputStream 输入流，可为 {@code null}
     * @param charset 字符集，不能为 {@code null}
     * @return 读取到的字符串；输入流为 {@code null} 时返回 {@code ""}
     * @throws IOException 读取失败；异常时仍会关闭输入流
     * @throws NullPointerException {@code charset} 为 {@code null}
     */
    public static String readString(InputStream inputStream, Charset charset) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder out = new StringBuilder(BUFFER_SIZE);
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(inputStream, charset);
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, charsRead);
            }
        } finally {
            close(inputStream);
            close(reader);
        }
        return out.toString();
    }


    // ---------------------- writeString 2 OutputStream ----------------------

    /**
     * 按 UTF-8 将字符串写入输出流。
     *
     * <p>等价于 {@code writeString(data, output, StandardCharsets.UTF_8)}。
     *
     * @param data 待写入字符串，可为 {@code null}（为 {@code null} 时不写入）
     * @param output 输出流，不能为 {@code null}
     * @throws IOException 写入失败
     */
    public static void writeString(final String data, final OutputStream output) throws IOException {
        writeString(data, output, StandardCharsets.UTF_8);
    }

    /**
     * 按指定字符集将字符串写入输出流。
     *
     * <p>{@code data} 为 {@code null} 时直接返回，不写入任何内容；
     * {@code charset} 为 {@code null} 时按 UTF-8 处理。仅写入字符串内容，
     * 不追加换行符，也不 flush 或关闭输出流，流的生命周期由调用方管理。
     *
     * @param data 待写入字符串，可为 {@code null}
     * @param output 输出流，不能为 {@code null}
     * @param charset 字符集，可为 {@code null}（按 UTF-8）
     * @throws IOException 写入失败
     */
    public static void writeString(final String data, final OutputStream output, Charset charset) throws IOException {
        if (data == null) {
            return;
        }
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        Channels.newChannel(output).write(charset.encode(data));
    }


    // ---------------------- build BufferedWriter ----------------------

    /**
     * 构建 UTF-8、{@link #BUFFER_SIZE} 缓冲的 {@link BufferedWriter}。
     *
     * <p>等价于
     * {@code newBufferedWriter(file, append, StandardCharsets.UTF_8, BUFFER_SIZE)}。
     *
     * @param file 目标文件，不能为 {@code null}
     * @param append {@code true} 追加；{@code false} 覆盖（文件不存在则创建）
     * @return 新建的 writer，由调用方负责 flush/close
     * @throws IOException 文件无法打开（父目录不存在时不会自动创建）
     */
    public static BufferedWriter newBufferedWriter(File file, boolean append) throws IOException {
        return newBufferedWriter(file, append, StandardCharsets.UTF_8, BUFFER_SIZE);
    }

    /**
     * 构建指定字符集与缓冲区大小的 {@link BufferedWriter}。
     *
     * <p>底层为 {@code FileOutputStream(file, append)}：{@code append=false}
     * 覆盖已有内容，{@code append=true} 追加；文件不存在时创建。不会自动创建
     * 父目录，父目录缺失时会抛出 {@link FileNotFoundException}。换行符不由本方法
     * 写入，需要时由调用方显式写出；返回的 writer 由调用方负责 flush/close。
     *
     * @param file 目标文件，不能为 {@code null}
     * @param append {@code true} 追加；{@code false} 覆盖
     * @param charset 字符集，不能为 {@code null}
     * @param bufferSize 缓冲区大小，单位字符
     * @return 新建的 writer，由调用方负责关闭
     * @throws IOException 文件无法打开
     */
    public static BufferedWriter newBufferedWriter(File file, boolean append, Charset charset, int bufferSize) throws IOException {
        return new BufferedWriter(
                new OutputStreamWriter(
                        new FileOutputStream(file, append),
                        charset),
                bufferSize);
    }


    // ---------------------- other ----------------------

}
