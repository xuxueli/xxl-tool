package com.xxl.tool.io;

import com.xxl.tool.core.ArrayTool;
import com.xxl.tool.core.AssertTool;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * File Tool
 *
 * @author xuxueli 2024-10-27
 */
public class FileTool {

    // ---------------------- create file ----------------------

    /**
     * create new file object, not actual file on disk
     *
     * @param path file path
     * @return file object
     */
    public static File file(String path) {
        return new File(path);
    }

    /**
     * create new file object, not actual file on disk
     *
     * @param parent parent path
     * @param child  child path
     * @return file object
     */
    public static File file(String parent, String child) {
        return new File(parent, child);
    }

    /**
     * create actual file on disk
     *
     * @param path file path
     * @return file
     */
    public static File createFile(String path) throws IOException {
        return createFile(file(path));
    }

    /**
     * create actual file on disk
     *
     * @param parent parent path
     * @param child  child path
     * @return file
     */
    public static File createFile(String parent, String child) throws IOException {
        return createFile(file(parent, child));
    }

    /**
     * create actual file on disk
     *
     * @param file file object
     * @return file
     */
    public static File createFile(File file) throws IOException {
        // valid
        if (file == null) {
            return null;
        }
        if (exists(file)) {
            return file;
        }

        // create parent dir
        createParentDirectories(file);

        // create new file
        try {
            Files.createFile(file.toPath());
        } catch (FileAlreadyExistsException e) {
            // exists pass
            return file;
        }
        //file.createNewFile();
        return file;
    }


    // ---------------------- create directories ----------------------

    /**
     * create parent directories of file
     *
     * 1、will create all necessary parent directories
     *
     * @param file file object
     */
    public static boolean createParentDirectories(File file) throws IOException {
        // valid
        if (file == null) {
            return false;
        }

        // valid parent-dir exist
        File parentFile = file.getParentFile();
        return createDirectories(parentFile);
    }

    /**
     * create directories
     *
     * 1、will create all necessary parent directories
     *
     * @param dir dir
     * @return true if and only if the directory was created, along with all necessary parent directories; false otherwise
     */
    public static boolean createDirectories(File dir) throws IOException {
        // valid
        if (dir == null) {
            return false;
        }

        // valid exist
        if (exists(dir)) {
            if (isDirectory(dir)) {
                return true;
            } else {
                throw new RuntimeException("file is not directory, path=" + dir.getPath());
            }
        }

        // create parent dir
        Files.createDirectories(dir.toPath());
        // dir.mkdirs();
        return true;
    }


    // ---------------------- valid base ----------------------

    /**
     * is directory
     *
     * @param path path of file
     * @return {@code true} if is directory
     */
    public static boolean isDirectory(String path) {
        return (null != path) && Files.isDirectory(Paths.get(path));
    }

    /**
     * is directory
     *
     * @param file file
     * @return {@code true} if is directory
     */
    public static boolean isDirectory(File file) {
        return (null != file) && Files.isDirectory(file.toPath());
    }

    /**
     * is file
     *
     * @param path path of file
     * @return {@code true} if is file
     */
    public static boolean isFile(String path) {
        return (null != path) && file(path).isFile();
    }

    /**
     * is file
     *
     * @param file file object
     * @return {@code true} if is file
     */
    public static boolean isFile(File file) {
        return (null != file) && file.isFile();
    }

    /**
     * is empty file or directory
     *
     * @param file file object
     * @return {@code true} if empty
     */
    public static boolean isEmpty(File file) {
        if (!exists(file)) {
            return true;
        }

        if (file.isDirectory()) {
            String[] subFiles = file.list();
            return ArrayTool.isEmpty(subFiles);
        } else if (file.isFile()) {
            return file.length() <= 0;
        } else {
            // not file or directory, may be link
            return false;
        }
    }

    /**
     * is not empty, file or directory
     *
     * @param file file object
     * @return {@code true} if is not empty
     */
    public static boolean isNotEmpty(File file) {
        return !isEmpty(file);
    }

    /**
     * is same file
     *
     * @param file1 file1 object
     * @param file2 file2 object
     * @return true if is same file
     */
    public static boolean isSameFile(File file1, File file2) throws IOException {
        if (file1 == null || file2 == null) {
            return false;
        }
        return Files.isSameFile(file1.toPath(), file2.toPath());
    }

    /**
     * is sub file of parent
     *
     * @param parent    parent file
     * @param sub       sub file
     * @return true if is sub file of parent
     */
    public static boolean isSub(Path parent, Path sub) {
        return toAbsoluteNormal(sub).startsWith(toAbsoluteNormal(parent));
    }


    // ---------------------- valid exists ----------------------

    /**
     * is file exist
     *
     * @param path path of file
     * @return true if file exist
     */
    public static boolean exists(String path) {
        return (null != path) && exists(file(path));
    }

    /**
     * whether a file exists
     *
     * @param file file object
     * @return true if file exist
     */
    public static boolean exists(File file) {
        if (null == file) {
            return false;
        }

        //file.exists();
        return Files.exists(file.toPath());
    }


    // ---------------------- info file-path ----------------------

    /**
     * to absolute normal path
     *
     * @param path file path
     * @return absolute normal path
     */
    public static Path toAbsoluteNormal(Path path) {
        AssertTool.notNull(path, "path is null");
        return path.toAbsolutePath().normalize();
    }

    /**
     * to absolute path
     *
     * @param path file path
     * @return absolute path
     */
    public static Path toAbsolutePath(Path path) {
        AssertTool.notNull(path, "path is null");
        return path.toAbsolutePath();
    }


    // ---------------------- info size ----------------------

    /**
     * get file size, not include directory size
     *
     * @param path              file path
     * @return size in bytes
     */
    public static long size(String path) {
        return size(file(path), false);
    }

    /**
     * get file size, not include directory size
     *
     * @param file file object
     * @return size in bytes
     */
    public static long size(File file) {
        return size(file, false);
    }

    /**
     * get file size
     *
     * @param file              file object
     * @param includeDirSize    include directory size
     * @return size in bytes
     */
    public static long size(File file, boolean includeDirSize) {
        if (!(exists(file)) || Files.isSymbolicLink(file.toPath())) {
            return 0;
        }

        if (file.isDirectory()) {
            long size = includeDirSize ? file.length() : 0;
            File[] subFiles = file.listFiles();
            if (ArrayTool.isEmpty(subFiles)) {
                return 0L;
            }
            for (File subFile : subFiles) {
                size += size(subFile, includeDirSize);
            }
            return size;
        } else {
            return file.length();
        }
    }


    // ---------------------- info line ----------------------

    // CR：回车符
    private static final char CR = '\r';
    // 含义：
    // LF：换行符
    private static final char LF = '\n';

    /**
     * get total lines
     *
     * @param path path of file
     * @return total line number
     */
    public static int totalLines(String path) {
        return totalLines(file(path), 1024, true);
    }

    /**
     * get total lines
     *
     * @param file file object
     * @return total line number
     */
    public static int totalLines(File file) {
        return totalLines(file, 1024, true);
    }

    /**
     * get total lines
     *
     * @param file                      file
     * @param bufferSize                buffer size
     * @param lastLineSeparatorAsLine   last line separator as line
     * @return total line number
     */
    public static int totalLines(File file, int bufferSize, boolean lastLineSeparatorAsLine) {
        // valid
        if (!isFile(file)) {
            throw new RuntimeException("file invalid");
        }
        if (bufferSize < 1) {
            bufferSize = 1024;
        }

        // calculate
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] buffer = new byte[bufferSize];

            // read first: valid empty file
            int bytesRead = is.read(buffer);
            if (bytesRead == -1) {
                return 0;
            }

            int lineCount = 1;
            byte previousChar = 0;
            byte currentChar = 0;

            // read with buffer
            while (bytesRead != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    previousChar = currentChar;
                    currentChar = buffer[i];
                    if (currentChar == LF || previousChar == CR) {
                        lineCount++;
                    }
                }
                bytesRead = is.read(buffer);
            }

            // last char
            if (lastLineSeparatorAsLine) {
                // read-buffer only process LF, should include CR
                if (currentChar == CR) {
                    lineCount++;
                }
            } else {
                // read-buffer already process LF, shoud subtracte 1
                if (currentChar == LF) {
                    lineCount--;
                }
            }

            return lineCount;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // ---------------------- delete + clean ----------------------

    /**
     * delete file or directory
     *
     * @param path path of file
     * @return true if success
     */
    public static boolean delete(String path){
        return delete(file(path));
    }

    /**
     * delete file or directory
     *
     * @param file file object
     * @return true if success
     */
    public static boolean delete(File file){
        // valid
        if (!exists(file)) {
            return true;
        }

        // if directory, clean sub file
        if (file.isDirectory()) {
            boolean cleanResult = clean(file);
            if (!cleanResult) {
                return false;
            }
        }

        // delete current file
        final Path path = file.toPath();
        try {
            Files.delete(path);
        } catch (AccessDeniedException e) {
            return file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * clean directory
     *
     * @param dirPath directory path
     * @return true if success
     */
    public static boolean clean(String dirPath){
        return clean(file(dirPath), false);
    }

    /**
     * clean directory
     *
     * @param directory directory file object
     * @return true if success
     */
    public static boolean clean(File directory) {
        return clean(directory, false);
    }

    /**
     * clean directory
     * @param directory  directory file object
     * @param skipDeleteFailFile true, skip delete fail file
     * @return
     */
    public static boolean clean(File directory, boolean skipDeleteFailFile) {
        // valid
        if (!exists(directory)) {
            return true;
        }

        if (!isDirectory(directory)) {
            return false;
        }

        // delete
        boolean result = true;
        final File[] files = directory.listFiles();
        if (ArrayTool.isNotEmpty(files)) {
            for (File childFile : files) {
                boolean deleteResult = delete(childFile);
                if (!deleteResult) {
                    result = false;
                    if (!skipDeleteFailFile) {
                        return false;
                    }
                }
            }
        }
        return result;
    }

    // ---------------------- copy ----------------------

    /**
     * copy file
     *
     * @param src           source file path
     * @param dest          destination file path or directory path
     * @param isOverride    true will override, false and dest-file exists will throw FileAlreadyExistsException
     * @return destination file
     */
    public static File copy(String src, String dest, boolean isOverride) throws IOException {
        AssertTool.notBlank(src, "source file path is blank");
        AssertTool.notBlank(dest, "destination file path is blank");

        return copy(Paths.get(src), Paths.get(dest), isOverride);
    }

    /**
     * copy file
     *
     * @param src           source file
     * @param dest          destination file  or directory
     * @param isOverride    true will override, false and dest-file exists will throw FileAlreadyExistsException
     * @return destination file
     */
    public static File copy(File src, File dest, boolean isOverride) throws IOException {
        AssertTool.notNull(src, "source file is null");
        AssertTool.notNull(dest, "destination file is null");

        return copy(src.toPath(), dest.toPath(), isOverride);
    }

    /**
     * copy file
     *
     * @param src           source file
     * @param dest          destination file  or directory
     * @param isOverride    true will override, false and dest-file exists will throw FileAlreadyExistsException
     * @return destination file
     */
    public static File copy(Path src, Path dest, boolean isOverride) throws IOException {
        AssertTool.notNull(src, "source file is null");
        AssertTool.notNull(dest, "destination file is null");

        StandardCopyOption[] copyOptions = isOverride
                ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING}
                : new StandardCopyOption[]{};

        return copy(src, dest, copyOptions);
    }

    /**
     * copy file
     *
     * 1、source must exists
     * 2、source only support file (todo: support directory)
     * 3、destination file can not be same as source file
     * 4、destination can not be sub of source
     *
     * @param src           source file path
     * @param dest          destination file path or directory path
     * @param options       copy options, such as StandardCopyOption.REPLACE_EXISTING、StandardCopyOption.COPY_ATTRIBUTES
     * @return destination file
     */
    public static File copy(Path src, Path dest, StandardCopyOption... options) throws IOException {
        AssertTool.notNull(src, "source file path is null");
        AssertTool.notNull(dest, "destination file file is null");

        final File srcFile = src.toFile();
        final File destFile = dest.toFile();

        // valid
        if (!exists(srcFile)) {
            throw new RuntimeException("source file not exists");
        }
        if (!isFile(srcFile)) {
            throw new RuntimeException("source only support file");
        }
        if (toAbsoluteNormal(srcFile.toPath()).equals(toAbsoluteNormal(destFile.toPath()))) {
            throw new RuntimeException("destination file can not be same as source file");
        }
        if (isSub(src, dest)) {
            throw new RuntimeException("destination can not be sub of source");
        }

        // build final dest-path
        final Path finalDestFilePath = isDirectory(destFile)
                ? dest.resolve(src.getFileName())       // build new file path
                : dest;

        // make parent dir
        createParentDirectories(destFile);

        // copy
        return Files.copy(src, finalDestFilePath, options).toFile();
    }


    // ---------------------- move ----------------------

    /**
     * move file
     *
     * @param src           source file path
     * @param dest          destination file path or directory path
     * @param isOverride    true will override, false and dest-file exists will throw FileAlreadyExistsException
     * @return destination file
     */
    public static File move(String src, String dest, boolean isOverride) throws IOException {
        AssertTool.notBlank(src, "source file path is blank");
        AssertTool.notBlank(dest, "destination file path is blank");

        return move(Paths.get(src), Paths.get(dest), isOverride);
    }

    /**
     * move file or directory
     *
     * @param src           source file
     * @param dest          destination file  or directory
     * @param isOverride    true will override, false and dest-file exists will throw FileAlreadyExistsException
     * @return destination file
     */
    public static File move(File src, File dest, boolean isOverride) throws IOException {
        AssertTool.notNull(src, "source file is null");
        AssertTool.notNull(dest, "destination file is null");

        return move(src.toPath(), dest.toPath(), isOverride);
    }

    /**
     * move file or directory
     *
     * @param src           source file
     * @param dest          destination file  or directory
     * @param isOverride    true will override, false and dest-file exists will throw FileAlreadyExistsException
     * @return destination file
     */
    public static File move(Path src, Path dest, boolean isOverride) throws IOException {
        AssertTool.notNull(src, "source file is null");
        AssertTool.notNull(dest, "destination file is null");

        StandardCopyOption[] copyOptions = isOverride
                ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING}
                : new StandardCopyOption[]{};

        return move(src, dest, copyOptions);
    }

    /**
     * move file or directory
     *
     * 1、source must exists
     * 2、destination file can not be same as source file
     * 3、destination can not be sub of source
     *
     * @param src           source file path
     * @param dest          destination file path or directory path
     * @param options       copy options, such as StandardCopyOption.REPLACE_EXISTING、StandardCopyOption.COPY_ATTRIBUTES
     * @return destination file
     */
    public static File move(Path src, Path dest, StandardCopyOption... options) throws IOException {
        AssertTool.notNull(src, "source file path is null");
        AssertTool.notNull(dest, "destination file file is null");

        final File srcFile = src.toFile();
        final File destFile = dest.toFile();

        // valid
        if (!exists(srcFile)) {
            throw new RuntimeException("source file not exists");
        }
        if (toAbsoluteNormal(srcFile.toPath()).equals(toAbsoluteNormal(destFile.toPath()))) {
            throw new RuntimeException("destination file can not be same as source file");
        }
        if (isSub(src, dest)) {
            throw new RuntimeException("destination can not be sub of source");
        }

        // is override
       /* boolean isOverride = options != null && Arrays.stream(options).anyMatch(option -> option == StandardCopyOption.REPLACE_EXISTING);
        if (!isOverride && exists(destFile)) {
            // not override, if exists return it
            return destFile;
        }*/

        // build final dest-path
        final Path finalDestFilePath = isDirectory(destFile)
                ? dest.resolve(src.getFileName())       // build new file path
                : dest;

        // make parent dir
        createParentDirectories(destFile);

        // move
        return Files.move(src, finalDestFilePath, options).toFile();
    }


    // ---------------------- file rename ----------------------

    /**
     * rename file
     *
     * @param file          origin file
     * @param newFileName   new file name
     * @param isOverride    true if override, false will skip
     * @return new file
     */
    public static File rename(File file, String newFileName, boolean isOverride) throws IOException {
        AssertTool.notNull(file, "file path is null");
        AssertTool.notBlank(newFileName, "newFileName is blank");

        return move(file.toPath(), file.toPath().resolveSibling(newFileName), isOverride);
    }


    // ---------------------- write data ----------------------

    /**
     * write string
     *
     * @param path      file path
     * @param content   file content
     * @throws IOException
     */
    public static void writeString(String path, String content) throws IOException {
        writeString(path, content, false, StandardCharsets.UTF_8);
    }

    /**
     * write string
     *
     * @param path      file path
     * @param content   file content
     * @param append    true if append, false will override
     */
    public static void writeString(String path, String content, boolean append) throws IOException {
        writeString(path, content, append, StandardCharsets.UTF_8);
    }

    /**
     * write string
     *
     * @param path      file path
     * @param content   file content
     * @param append    true if append, false will override
     * @param charset   charset encoding
     */
    public static void writeString(String path, String content, boolean append, Charset charset) throws IOException {
        // valid
        AssertTool.notBlank(path, "file path is null");

        // check content
        if (content == null) {
            return;
        }
        // check charset
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // check and build file
        File file = file(path);
        if (!exists(path)) {
            createFile(file);
        } else if (!isFile(file) ) {
            throw new RuntimeException("path("+ path +") is not a file");
        }

        /*
        OpenOption[] optionLists = append
                ? new OpenOption[]{ StandardOpenOption.CREATE, StandardOpenOption.APPEND }
                : new OpenOption[]{ StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };

        // 1、big file：Files.newBufferedWriter
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(path),
                StandardCharsets.UTF_8,
                optionLists)) {
            writer.write(content);
        }

        // 2、small file：Files.writeString
        Files.writeString(
                Paths.get(path),
                content,
                StandardCharsets.UTF_8,
                optionLists
        );

        // 3、复杂：Channels
        try (OutputStream output = Files.newOutputStream(Paths.get(path), optionLists)) {
            Channels.newChannel(output).write(charset.encode(content));
        }
        */

        // do write, support big file
        BufferedWriter writer = null;
        try {
            writer = IOTool.newBufferedWriter(file, append, charset, IOTool.BUFFER_SIZE);
            writer.write(content);
            writer.flush();
        } finally {
            IOTool.close(writer);
        }
    }


    // ---------------------- write line ----------------------

    /**
     * write lines
     *
     * 1、default append
     *
     * @param path              file path
     * @param lines             lines content
     * @throws IOException exception
     */
    public static void writeLines(String path, Iterable<?> lines) throws IOException {
        writeLines(path, lines, null, true, null);
    }

    /**
     * write lines
     *
     * @param path              file path
     * @param lines             lines content
     * @param append            true if append, false will override
     * @throws IOException exception
     */
    public static void writeLines(String path, Iterable<?> lines, boolean append) throws IOException {
        writeLines(path, lines, null, append, null);
    }

    /**
     * write lines
     *
     * @param path              file path
     * @param lines             lines content
     * @param lineSeparator     line separator
     * @param append            true if append, false will override
     * @param charset           charset encoding
     * @throws IOException exception
     */
    public static void writeLines(String path, Iterable<?> lines, String lineSeparator, boolean append, Charset charset) throws IOException {
        // valid
        AssertTool.notBlank(path, "file path is null");

        // check lines
        if (lines==null || !lines.iterator().hasNext()) {
            return;
        }
        // check lineSeparator
        if (lineSeparator == null) {
            lineSeparator = System.lineSeparator();
        }
        // check charset
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // check and build file
        File file = file(path);
        if (!exists(path)) {
            createFile(file);
        } else if (!isFile(file) ) {
            throw new RuntimeException("path("+ path +") is not a file");
        }

        // do write, support big file
        BufferedWriter writer = null;
        try {
            writer = IOTool.newBufferedWriter(file, append, charset, IOTool.BUFFER_SIZE);
            for (final Object line : lines) {
                if (line != null) {
                    writer.write(line.toString());
                }
                writer.write(lineSeparator);
            }
            writer.flush();
        } finally {
            IOTool.close(writer);
        }

    }


    // ---------------------- write line with Supplier ----------------------

    /**
     * write lines
     *
     * 1、default append
     *
     * @param path              file path
     * @param lineSupplier      line supplier
     * @throws IOException exception
     */
    public static void writeLines(String path, Supplier<?> lineSupplier) throws IOException {
        writeLines(path, lineSupplier, null, true, null);
    }

    /**
     * write lines
     *
     * @param path              file path
     * @param lineSupplier      line supplier
     * @param append            true if append, false will override
     * @throws IOException exception
     */
    public static void writeLines(String path, Supplier<?> lineSupplier, boolean append) throws IOException {
        writeLines(path, lineSupplier, null, append, null);
    }

    /**
     * write lines
     *
     * @param path              file path
     * @param lineSupplier      line supplier
     * @param lineSeparator     line separator
     * @param append            true if append, false will override
     * @param charset           charset encoding
     * @throws IOException exception
     */
    public static void writeLines(String path, Supplier<?> lineSupplier, String lineSeparator, boolean append, Charset charset) throws IOException {
        // valid
        AssertTool.notBlank(path, "file path is null");
        AssertTool.notNull(lineSupplier, "supplier is null");

        // check lineSeparator
        if (lineSeparator == null) {
            lineSeparator = System.lineSeparator();
        }
        // check charset
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // check and build file
        File file = file(path);
        if (!exists(path)) {
            createFile(file);
        } else if (!isFile(file) ) {
            throw new RuntimeException("path("+ path +") is not a file");
        }

        // do write, support big file
        BufferedWriter writer = null;
        try {
            writer = IOTool.newBufferedWriter(file, append, charset, IOTool.BUFFER_SIZE);
            Object line;
            while ((line = lineSupplier.get()) != null) {
                writer.write(line.toString());
                writer.write(lineSeparator);
            }
            writer.flush();
        } finally {
            IOTool.close(writer);
        }

    }


    // ---------------------- read String ----------------------

    /**
     * read string
     *
     * @param path      file path
     * @return file data string
     */
    public static String readString(String path) throws IOException {
        return readString(path, StandardCharsets.UTF_8);
    }

    /**
     * read string
     *
     * @param path      file path
     * @param charset   charset encoding
     * @return file data string
     */
    public static String readString(String path, Charset charset) throws IOException {
        // valid
        AssertTool.notBlank(path, "file path is null");

        // check charset
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // check file
        if (!exists(path)) {
            return null;
        }

        /*
        // 1、readBytes
        String result = null;
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            byte[] dataBytes = IOTool.readBytes(inputStream);
            result = new String(dataBytes, charset);
        }

        // 2、readBytes
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            result = IOTool.readString(inputStream, charset);
        }
        */

        // read string
        return Files.readString(Paths.get(path), charset);
    }


    // ---------------------- read Lines ----------------------

    /**
     * read lines
     *
     * @param path      file path
     * @return file data lines
     * @throws IOException exception
     */
    public static List<String> readLines(String path) throws IOException {
        return readLines(path, StandardCharsets.UTF_8);
    }

    /**
     * read lines
     *
     * @param path      file path
     * @param charset   charset encoding
     * @return file data lines
     * @throws IOException exception
     */
    public static List<String> readLines(String path, Charset charset) throws IOException {
        // valid
        AssertTool.notBlank(path, "file path is null");

        // check charset
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // check file
        if (!exists(path)) {
            return new ArrayList<>();
        }

        /*
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), charset)) {
            List<String> result = new ArrayList<>();
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                result.add(line);
            }
            return result;
        }
        */

        // do read
        return Files.readAllLines(Paths.get(path), charset);
    }


    // ---------------------- read Lines with consumer ----------------------

    /**
     * read lines with consumer
     *
     * @param path              file path
     * @param lineConsumer      line consumer
     * @throws IOException exception
     */
    public static void readLines(String path, Consumer<String> lineConsumer) throws IOException {
        readLines(path, StandardCharsets.UTF_8, lineConsumer);
    }

    /**
     * read lines with consumer
     *
     * @param path              file path
     * @param charset           charset encoding
     * @param lineConsumer      line consumer
     * @throws IOException exception
     */
    public static void readLines(String path, Charset charset, Consumer<String> lineConsumer) throws IOException {
        // valid
        AssertTool.notBlank(path, "file path is null");
        AssertTool.notNull(lineConsumer, "lineConsumer is null");

        // check charset
        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        // check file
        if (!exists(path)) {
            return;
        }

        // do read
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(path), charset)) {
            for (;;) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                lineConsumer.accept(line);
            }
        }
    }

    // ---------------------- other ----------------------

}
