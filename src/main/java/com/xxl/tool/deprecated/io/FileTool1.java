//package com.xxl.tool.core;
//
//import com.xxl.tool.io.IOTool2;
//
//import java.io.*;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.*;
//import java.util.*;
//
///**
// * File Tool
// *
// * @author xuxueli 2024-01-21 05:03:10
// */
//public class FileTool1 {
//
//
//    // ---------------------- base ----------------------
//
//    /**
//     * Gets the parent of the given file.
//     *
//     * @param file      The file to query, may be null.
//     * @return          The parent file or {@code null}. Note that a file's parent may be null as well.
//     */
//    public static File getParentFile(final File file) {
//        return file == null ? null : file.getParentFile();
//    }
//
//    /**
//     * Creates all directories for a File object
//     *
//     * @param directory         directory to create; including any necessary but non-existent parent directories.
//     */
//    public static void mkdirs(final File directory) throws IOException {
//        if (directory != null && !directory.mkdirs() && !directory.isDirectory()) {
//            throw new IOException("Cannot create directory '" + directory + "'.");
//        }
//    }
//
//    /**
//     * Creates the parent directories of the given {@link File}
//     *
//     * @param file      file to create, if they don't already exist.
//     * @return          file of the parent directory
//     */
//    public static void createParentDirectories(final File file) throws IOException {
//        mkdirs(getParentFile(file));
//    }
//
//    // ---------------------- copy ----------------------
//
//    /**
//     * Copy a file to a destination.
//     */
//    public static void copyFile(final String srcFilePath, final String destFilePath, CopyOption... copyOptions) throws IOException {
//        AssertTool.notNull(srcFilePath, "No srcFile File specified");
//        AssertTool.notNull(destFilePath, "No destFile File specified");
//
//        File srcFile = new File(srcFilePath);
//        File destFile = new File(destFilePath);
//
//        // check file
//        if (!srcFile.exists() || !srcFile.isFile()) {
//            throw new IOException("File '" + srcFile + "' does not exist or is not a file");
//        }
//
//        // create dest parent file
//        createParentDirectories(destFile);
//
//        // copyOptions
//        if (copyOptions==null || copyOptions.length==0) {
//            // default replace
//            copyOptions = new CopyOption[]{StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES};
//        }
//
//        // copy
//        Files.copy(srcFile.toPath(), destFile.toPath(), copyOptions);
//    }
//
//    /**
//     * Copy file from srcFile to destFile
//     *
//     * @param srcFile       src file
//     * @param destFile      desc file
//     * @param copyOptions   copy options, like {@link java.nio.file.StandardCopyOption}
//     */
//    public static void copyFile(final File srcFile, final File destFile, final CopyOption... copyOptions) throws IOException {
//        AssertTool.notNull(srcFile, "No srcFile File specified");
//        AssertTool.notNull(destFile, "No destFile File specified");
//
//        // check file
//        if (!srcFile.exists() || !srcFile.isFile()) {
//            throw new IOException("File '" + srcFile + "' does not exist or is not a file");
//        }
//
//        // create dest parent file
//        createParentDirectories(destFile);
//
//        // copy
//        Files.copy(srcFile.toPath(), destFile.toPath(), copyOptions);
//    }
//
//    /**
//     * Copy file data from srcFile to destFile
//     *
//     * @param srcFilePath       src file
//     * @param destFilePath      desc file
//     */
//    public static int copyFileData(String srcFilePath, String destFilePath) throws IOException {
//        AssertTool.notNull(srcFilePath, "No srcFile File specified");
//        AssertTool.notNull(destFilePath, "No destFile File specified");
//
//        File srcFile = new File(srcFilePath);
//        File destFile = new File(destFilePath);
//
//        return IOTool2.copy(Files.newInputStream(srcFile.toPath()), Files.newOutputStream(destFile.toPath()));
//    }
//
//    /**
//     * Copy file data from srcFile to destFile
//     *
//     * @param srcFile       src file
//     * @param destFile      desc file
//     */
//    public static int copyFileData(File srcFile, File destFile) throws IOException {
//        AssertTool.notNull(srcFile, "No srcFile File specified");
//        AssertTool.notNull(destFile, "No destFile File specified");
//        return IOTool2.copy(Files.newInputStream(srcFile.toPath()), Files.newOutputStream(destFile.toPath()));
//    }
//
//    // ---------------------- write ----------------------
//
//    /**
//     * Write lines to a file
//     *
//     * @param filePath  the file to write to
//     * @param lines the lines to write
//     */
//    public static void writeLines(final String filePath, final Collection<?> lines) throws IOException {
//        writeLines(new File(filePath), null, lines, null, false);
//    }
//
//    /**
//     * Write lines to a file
//     *
//     * @param filePath      the file to write to
//     * @param lines         the lines to write
//     * @param append        if {@code true}, then bytes will be added to the end of the file rather than overwriting
//     */
//    public static void writeLines(final String filePath, final Collection<?> lines, final boolean append) throws IOException {
//        writeLines(new File(filePath), null, lines, null, append);
//    }
//
//    /**
//     * Writes the {@code toString()} value of each item in a collection tothe specified {@link File} line by line.
//     *
//     * @param file          the file to write to
//     * @param charset       the encoding to use, {@code null} means platform default
//     * @param lines         the lines to write, {@code null} entries produce blank lines
//     * @param lineEnding    the line separator to use, {@code null} is system default
//     * @param append        if {@code true}, then bytes will be added to the end of the file rather than overwriting
//     */
//    public static void writeLines(final File file,
//                                  final Charset charset,
//                                  final Collection<?> lines,
//                                  final String lineEnding,
//                                  final boolean append)
//            throws IOException {
//        try (OutputStream out = new BufferedOutputStream(newOutputStream(file, append))) {
//            IOTool2.writeLines(lines, lineEnding, out, charset);
//        }
//    }
//
//    private static final OpenOption[] OPEN_OPTIONS_APPEND = { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
//    private static final OpenOption[] OPEN_OPTIONS_TRUNCATE = { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
//    /**
//     * Creates a new {@link OutputStream} for the specified file.
//     */
//    private static OutputStream newOutputStream(final File file, final boolean append) throws IOException {
//
//        // path
//        Path path = Objects.requireNonNull(file, "file not exists").toPath();
//
//        // open option
//        List<OpenOption> optionList = new ArrayList<>(Arrays.asList(append ? OPEN_OPTIONS_APPEND : OPEN_OPTIONS_TRUNCATE));
//        /*LinkOption[] linkOptions = {};
//        optionList.addAll(Arrays.asList(linkOptions != null ? linkOptions : EMPTY_LINK_OPTION_ARRAY)); // default null*/
//
//        return Files.newOutputStream(path, optionList.toArray(new OpenOption[0]));
//    }
//
//    /**
//     * Writes a String to a file
//     *
//     * @param filePath  the file to write
//     * @param data      the content to write to the file
//     */
//    public static void writeString(final String filePath, final String data) throws IOException {
//        writeString(new File(filePath), data, Charset.defaultCharset(), false);
//    }
//
//    /**
//     * Writes a String to a file
//     *
//     * @param filePath  the file to write
//     * @param data      the content to write to the file
//     * @param append    if {@code true}, then bytes will be added to the end of the file rather than overwriting
//     */
//    public static void writeString(final String filePath, final String data, final boolean append) throws IOException {
//        writeString(new File(filePath), data, Charset.defaultCharset(), append);
//    }
//
//    /**
//     * Writes a String to a file, creating the file if it does not exist.
//     *
//     * @param file      the file to write
//     * @param data      the content to write to the file
//     * @param charset   the encoding to use, {@code null} means platform default
//     * @param append    if {@code true}, then bytes will be added to the end of the file rather than overwriting
//     */
//    public static void writeString(final File file, final String data, Charset charset, final boolean append) throws IOException {
//        if (charset == null) {
//            charset = StandardCharsets.UTF_8;
//        }
//
//        try (OutputStream out = newOutputStream(file, append)) {
//            IOTool2.writeString(data, out, charset);
//        }
//    }
//
//    // ---------------------- other ----------------------
//
//
//}
