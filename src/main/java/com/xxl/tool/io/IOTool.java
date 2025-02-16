package com.xxl.tool.io;

import com.xxl.tool.core.AssertTool;

import java.io.*;
import java.nio.charset.Charset;

public class IOTool {

    /**
     * The default buffer size used when copying bytes.
     */
    public static final int BUFFER_SIZE = 1024 * 4;


    // ---------------------- copy ----------------------

    /**
     * Copy from InputStream to OutputStream, Closes both streams when done.
     *
     * @param in InputStream
     * @param out OutputStream
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    public static int copy(InputStream in, OutputStream out) throws IOException {
        AssertTool.notNull(in, "No InputStream specified");
        AssertTool.notNull(out, "No OutputStream specified");

        try {
            int byteCount = 0;
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            close(in);
            close(out);
        }
    }

    /**
     * Copy from byte array to OutputStream, Closes the stream when done.
     *
     * @param in the byte array
     * @param out OutputStream
     * @throws IOException in case of I/O errors
     */
    public static void copy(byte[] in, OutputStream out) throws IOException {
        AssertTool.notNull(in, "No input byte array specified");
        AssertTool.notNull(out, "No OutputStream specified");

        try {
            out.write(in);
        }
        finally {
            close(out);
        }
    }

    /**
     * Copy the contents of the given InputStream into a new byte array.
     * Closes the stream when done.
     *
     * @param in the stream to copy from (may be {@code null} or empty)
     * @return the new byte array that has been copied to (possibly empty)
     * @throws IOException in case of I/O errors
     */
    public static byte[] copyToByteArray(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        copy(in, out);
        return out.toByteArray();
    }

    /**
     * Copy the contents of the given InputStream into a String.
     *
     * @param in
     * @param charset
     * @return
     * @throws IOException
     */
    public static String copyToString(InputStream in, Charset charset) throws IOException {
        if (in == null) {
            return "";
        }

        StringBuilder out = new StringBuilder(BUFFER_SIZE);
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in, charset);
            char[] buffer = new char[BUFFER_SIZE];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                out.append(buffer, 0, charsRead);
            }
        } finally {
            close(in);
            close(reader);
        }
        return out.toString();
    }

    /**
     * Attempt to close the supplied {@link Closeable}, ignore exceptions
     *
     * @param closeable the {@code Closeable} to close
     */
    private static void close(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        }
        catch (IOException ex) {
            // ignore
        }
    }

    // ---------------------- other ----------------------


}
