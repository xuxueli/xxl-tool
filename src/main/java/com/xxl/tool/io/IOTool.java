package com.xxl.tool.io;

import com.xxl.tool.core.AssertTool;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * IO Tool
 *
 * @author xuxueli 2024-10-27
 */
public class IOTool {

    /**
     * default buffer size
     */
    public static final int BUFFER_SIZE = 1024 * 8;


    // ---------------------- copy ----------------------

    /**
     * copy from InputStream to OutputStream, close both streams when done.
     *
     * @param input     InputStream
     * @param output    OutputStream
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, true,  true);
    }

    /**
     * copy from InputStream to OutputStream,
     *
     * @param input         InputStream
     * @param output        OutputStream
     * @param closeInput    true to close input stream when done; false to keep it open
     * @param closeOutput   true to close output stream when done; false to keep it open
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
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
     * copy from InputStream to OutputStream
     *
     * 1、will close output streams when done.
     *
     * @param input    the byte array
     * @param output   OutputStream
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] input, OutputStream output) throws IOException {
        copy(input, output, true);
    }

    /**
     * copy from InputStream to OutputStream
     *
     * @param input    the byte array
     * @param output   OutputStream
     * @param closeOutput   true to close output streams when done; false to keep them open
     * @throws IOException in case of I/O errors
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
     * close the supplied {@link Closeable}, ignore exceptions
     *
     * @param closeable the {@code Closeable} to close
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
     * read byte[] from InputStream
     *
     * 1、closes the stream when done.
     *
     * @param inputStream   input stream
     * @return read byte[]
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
     * read string contents from InputStream
     *
     * 1、closes the stream when done.
     *
     * @param inputStream   input stream
     * @return read string
     */
    public static String readString(InputStream inputStream) throws IOException {
        return readString(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * read string contents from InputStream
     *
     * 1、closes the stream when done.
     *
     * @param inputStream   input stream
     * @param charset       charset encoding
     * @return read string
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
     * write string 2 OutputStream
     *
     * @param data      string data
     * @param output    output stream
     * @throws IOException exception
     */
    public static void writeString(final String data, final OutputStream output) throws IOException {
        writeString(data, output, StandardCharsets.UTF_8);
    }

    /**
     * write string 2 OutputStream
     *
     * @param data      string data
     * @param output    output stream
     * @param charset   charset encoding
     * @throws IOException in case of I/O errors
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
     * build new writer
     *
     * @param file      file to write
     * @param append    true if append, false will override
     * @return  writer
     */
    public static BufferedWriter newBufferedWriter(File file, boolean append) throws IOException {
        return newBufferedWriter(file, append, StandardCharsets.UTF_8, BUFFER_SIZE);
    }

    /**
     * build new writer
     *
     * @param file          file to write
     * @param append        true if append, false will override
     * @param charset       charset to write
     * @param bufferSize    buffer size
     * @return writer
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
