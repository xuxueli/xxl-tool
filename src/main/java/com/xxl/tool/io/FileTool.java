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
 * 文件工具
 *
 * <p>覆盖文件/目录的创建、判断、路径、大小、行数、删除、复制、移动、重命名
 * 以及按字符串/行读写等能力：
 * <ul>
 *     <li>路径统一通过 {@link File}/{@link Path} 构造，避免手工拼接路径分隔符；
 *     相对路径按 JVM 工作目录解析，与 {@link File} 语义一致；</li>
 *     <li>写文件默认 UTF-8；{@code append=false} 覆盖、{@code true} 追加；
 *     需要时自动创建文件及其父目录；</li>
 *     <li>按行写入使用缓冲 writer 流式落盘，按 {@link java.util.function.Consumer}
 *     读取时逐行流式处理，适合大文件；{@link #readString(String)}/
 *     {@link #readLines(String)} 则一次性读入内存；</li>
 *     <li>方法内部打开的流均在返回前关闭；工具类无状态，方法可并发调用，
 *     但同一文件的并发写需调用方自行协调。</li>
 * </ul>
 *
 * @author xuxueli 2024-10-27
 */
public class FileTool {

    // ---------------------- create file ----------------------

    /**
     * 创建文件对象，不落盘。
     *
     * @param path 文件路径
     * @return 文件对象（此时磁盘上可能并不存在）
     */
    public static File file(String path) {
        return new File(path);
    }

    /**
     * 按父路径与子路径创建文件对象，不落盘。
     *
     * @param parent 父路径
     * @param child 子路径
     * @return 文件对象（此时磁盘上可能并不存在）
     */
    public static File file(String parent, String child) {
        return new File(parent, child);
    }

    /**
     * 创建文件及其父目录。
     *
     * <p>等价于 {@code createFile(file(path))}。
     *
     * @param path 文件路径
     * @return 创建后的文件对象
     * @throws IOException 创建父目录或文件失败
     */
    public static File createFile(String path) throws IOException {
        return createFile(file(path));
    }

    /**
     * 按父路径与子路径创建文件及其父目录。
     *
     * <p>等价于 {@code createFile(file(parent, child))}。
     *
     * @param parent 父路径
     * @param child 子路径
     * @return 创建后的文件对象
     * @throws IOException 创建父目录或文件失败
     */
    public static File createFile(String parent, String child) throws IOException {
        return createFile(file(parent, child));
    }

    /**
     * 创建文件及其所有必要的父目录。
     *
     * <p>文件已存在时直接返回；不存在时先创建父目录（见
     * {@link #createParentDirectories(File)}），再创建空文件。并发创建导致的
     * {@link FileAlreadyExistsException} 视为成功。参数为 {@code null} 时返回
     * {@code null}。
     *
     * @param file 文件对象，可为 {@code null}
     * @return 传入的文件对象；参数为 {@code null} 时返回 {@code null}
     * @throws IOException 创建父目录或文件失败
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
     * 创建文件的所有父目录。
     *
     * <p>取 {@link File#getParentFile()} 后交给 {@link #createDirectories(File)}，
     * 会递归创建缺失的上级目录。文件没有父路径（如单文件名的相对路径）或参数为
     * {@code null} 时返回 {@code false}。
     *
     * @param file 文件对象，可为 {@code null}
     * @return 父目录已存在或创建成功返回 {@code true}；无父目录/参数为 {@code null} 返回 {@code false}
     * @throws IOException 创建目录失败
     * @throws RuntimeException 父路径存在但不是目录
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
     * 创建目录（含所有必要的上级目录）。
     *
     * <p>目录已存在时返回 {@code true}；参数为 {@code null} 时返回 {@code false}；
     * 同名路径存在但不是目录时抛出 {@link RuntimeException}。
     *
     * @param dir 目录对象，可为 {@code null}
     * @return 目录已存在或创建成功返回 {@code true}；参数为 {@code null} 返回 {@code false}
     * @throws IOException 创建目录失败
     * @throws RuntimeException 同名路径存在但不是目录
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
     * 判断是否为目录。
     *
     * @param path 文件路径，可为 {@code null}
     * @return 是目录返回 {@code true}；路径为 {@code null} 或不存在返回 {@code false}
     */
    public static boolean isDirectory(String path) {
        return (null != path) && Files.isDirectory(Paths.get(path));
    }

    /**
     * 判断是否为目录。
     *
     * @param file 文件对象，可为 {@code null}
     * @return 是目录返回 {@code true}；参数为 {@code null} 或不存在返回 {@code false}
     */
    public static boolean isDirectory(File file) {
        return (null != file) && Files.isDirectory(file.toPath());
    }

    /**
     * 判断是否为普通文件（不跟随目录、不判断可读性）。
     *
     * @param path 文件路径，可为 {@code null}
     * @return 是普通文件返回 {@code true}；路径为 {@code null} 或不存在返回 {@code false}
     */
    public static boolean isFile(String path) {
        return (null != path) && file(path).isFile();
    }

    /**
     * 判断是否为普通文件（不跟随目录、不判断可读性）。
     *
     * @param file 文件对象，可为 {@code null}
     * @return 是普通文件返回 {@code true}；参数为 {@code null} 或不存在返回 {@code false}
     */
    public static boolean isFile(File file) {
        return (null != file) && file.isFile();
    }

    /**
     * 判断文件或目录是否为空。
     *
     * <p>语义：不存在返回 {@code true}；空目录（无子项）返回 {@code true}；
     * 文件长度为 0 返回 {@code true}；既不是普通文件也不是目录（如断开的符号
     * 链接、设备文件）返回 {@code false}。参数为 {@code null} 时按不存在处理。
     *
     * @param file 文件或目录对象，可为 {@code null}
     * @return 空或不存在返回 {@code true}
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
     * 判断文件或目录是否非空。
     *
     * <p>语义与 {@link #isEmpty(File)} 相反；参数为 {@code null} 时返回
     * {@code false}。
     *
     * @param file 文件或目录对象，可为 {@code null}
     * @return 非空返回 {@code true}
     */
    public static boolean isNotEmpty(File file) {
        return !isEmpty(file);
    }

    /**
     * 判断两个路径是否指向同一文件。
     *
     * <p>任一参数为 {@code null} 时返回 {@code false}；底层使用
     * {@link Files#isSameFile(Path, Path)}，文件不存在时通常返回 {@code false}，
     * 也可能因无法访问文件系统抛出 {@link IOException}。
     *
     * @param file1 文件 1，可为 {@code null}
     * @param file2 文件 2，可为 {@code null}
     * @return 指向同一文件返回 {@code true}
     * @throws IOException 无法访问文件系统时
     */
    public static boolean isSameFile(File file1, File file2) throws IOException {
        if (file1 == null || file2 == null) {
            return false;
        }
        return Files.isSameFile(file1.toPath(), file2.toPath());
    }

    /**
     * 判断 {@code sub} 是否位于 {@code parent} 之下（含两路径相等）。
     *
     * <p>先转为绝对路径并 normalize（消除 {@code .}/{@code ..}），再比较前缀；
     * 仅做路径比较，不要求路径真实存在，也不解析符号链接。
     *
     * @param parent 父路径，不能为 {@code null}
     * @param sub 子路径，不能为 {@code null}
     * @return {@code sub} 等于或以 {@code parent} 为前缀返回 {@code true}
     * @throws IllegalArgumentException 任一参数为 {@code null}
     */
    public static boolean isSub(Path parent, Path sub) {
        return toAbsoluteNormal(sub).startsWith(toAbsoluteNormal(parent));
    }


    // ---------------------- valid exists ----------------------

    /**
     * 判断路径是否存在。
     *
     * <p>可判断文件、目录或符号链接；路径为 {@code null} 时返回 {@code false}。
     *
     * @param path 文件路径，可为 {@code null}
     * @return 存在返回 {@code true}
     */
    public static boolean exists(String path) {
        return (null != path) && exists(file(path));
    }

    /**
     * 判断路径是否存在。
     *
     * <p>可判断文件、目录或符号链接；参数为 {@code null} 时返回 {@code false}。
     *
     * @param file 文件对象，可为 {@code null}
     * @return 存在返回 {@code true}
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
     * 转为绝对路径并规范化（消除 {@code .}/{@code ..}）。
     *
     * @param path 路径，不能为 {@code null}
     * @return 规范化后的绝对路径
     * @throws IllegalArgumentException 参数为 {@code null}
     */
    public static Path toAbsoluteNormal(Path path) {
        AssertTool.notNull(path, "path is null");
        return path.toAbsolutePath().normalize();
    }

    /**
     * 转为绝对路径（不做规范化，保留 {@code .}/{@code ..}）。
     *
     * @param path 路径，不能为 {@code null}
     * @return 绝对路径
     * @throws IllegalArgumentException 参数为 {@code null}
     */
    public static Path toAbsolutePath(Path path) {
        AssertTool.notNull(path, "path is null");
        return path.toAbsolutePath();
    }


    // ---------------------- info size ----------------------

    /**
     * 获取文件大小，目录仅统计其子项大小。
     *
     * <p>等价于 {@code size(file(path), false)}。
     *
     * @param path 文件路径
     * @return 字节数；路径不存在或为符号链接时返回 0
     */
    public static long size(String path) {
        return size(file(path), false);
    }

    /**
     * 获取文件大小，目录仅统计其子项大小。
     *
     * <p>等价于 {@code size(file, false)}。
     *
     * @param file 文件对象
     * @return 字节数；参数为 {@code null}、不存在或为符号链接时返回 0
     */
    public static long size(File file) {
        return size(file, false);
    }

    /**
     * 获取文件大小，可指定是否累计目录自身大小。
     *
     * <p>普通文件返回 {@link File#length()}；目录递归累加所有子项，若
     * {@code includeDirSize=true} 且目录非空，还会加上目录本身
     * {@link File#length()}（该值依赖文件系统，通常不等于子项之和）；空目录
     * 返回 0。参数为 {@code null}、不存在或为符号链接时返回 0。
     *
     * @param file 文件或目录对象，可为 {@code null}
     * @param includeDirSize 是否包含目录自身大小
     * @return 字节数；不存在返回 0
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
     * 获取文件总行数。
     *
     * <p>等价于 {@code totalLines(file(path), 1024, true)}。
     *
     * @param path 文件路径
     * @return 行数；空文件返回 0
     * @throws RuntimeException 文件不存在或不是普通文件
     */
    public static int totalLines(String path) {
        return totalLines(file(path), 1024, true);
    }

    /**
     * 获取文件总行数（末尾换行算作一行）。
     *
     * <p>等价于 {@code totalLines(file, 1024, true)}。
     *
     * @param file 文件对象
     * @return 行数；空文件返回 0
     * @throws RuntimeException 文件不存在或不是普通文件
     */
    public static int totalLines(File file) {
        return totalLines(file, 1024, true);
    }

    /**
     * 按指定缓冲区大小统计文件行数。
     *
     * <p>逐块读取字节（不会一次性载入），按 LF 换行，并把 CRLF 视为一次换行：
     * <ul>
     *     <li>{@code lastLineSeparatorAsLine=true}：末尾换行符额外算作一行，
     *     {@code "a\nb\n"} 计 3 行；</li>
     *     <li>{@code lastLineSeparatorAsLine=false}：{@code "a\nb\n"} 计 2 行。</li>
     * </ul>
     * {@code bufferSize} 小于 1 时按 1024 处理。文件不存在或不是普通文件时
     * 抛出 {@link RuntimeException}；读取流由 try-with-resources 关闭。
     *
     * @param file 文件对象
     * @param bufferSize 读取缓冲区大小，单位字节；小于 1 时按 1024
     * @param lastLineSeparatorAsLine 末尾换行符是否算作一行
     * @return 行数；空文件返回 0
     * @throws RuntimeException 文件不存在、不是普通文件或读取失败
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
     * 删除文件或目录（目录递归删除子项）。
     *
     * <p>等价于 {@code delete(file(path))}。
     *
     * @param path 文件/目录路径
     * @return 删除成功或本就不存在返回 {@code true}
     * @throws RuntimeException 删除过程中发生未处理的 I/O 异常
     */
    public static boolean delete(String path){
        return delete(file(path));
    }

    /**
     * 删除文件或目录（目录递归删除子项）。
     *
     * <p>路径不存在视为删除成功；目录先清空再删除自身。删除失败时返回
     * {@code false}（{@link AccessDeniedException} 会回退到
     * {@link File#delete()}），其他 I/O 异常包装为 {@link RuntimeException}。
     *
     * @param file 文件或目录对象
     * @return 删除成功或本就不存在返回 {@code true}
     * @throws RuntimeException 删除过程中发生未处理的 I/O 异常
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
        } catch (NoSuchFileException e) {
            return true;    // may be deleted by other process
        } catch (AccessDeniedException e) {
            return file.delete();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }

    /**
     * 清空目录下的直接子项（子目录会递归删除），保留目录本身。
     *
     * <p>等价于 {@code clean(file(dirPath), false)}。
     *
     * @param dirPath 目录路径
     * @return 全部删除成功返回 {@code true}；路径不是目录或存在删除失败项返回 {@code false}
     */
    public static boolean clean(String dirPath){
        return clean(file(dirPath), false);
    }

    /**
     * 清空目录下的直接子项（子目录会递归删除），保留目录本身。
     *
     * <p>等价于 {@code clean(directory, false)}。
     *
     * @param directory 目录对象
     * @return 全部删除成功返回 {@code true}；路径不是目录或存在删除失败项返回 {@code false}
     */
    public static boolean clean(File directory) {
        return clean(directory, false);
    }

    /**
     * 清空目录下的直接子项（子目录会递归删除），保留目录本身。
     *
     * <p>只处理直接子项，不删除 {@code directory} 本身；目录不存在返回
     * {@code true}，路径存在但不是目录返回 {@code false}。遇到删除失败时：
     * {@code skipDeleteFailFile=true} 跳过并继续，最终返回 {@code false}；
     * 为 {@code false} 时立即返回 {@code false}。
     *
     * @param directory 目录对象
     * @param skipDeleteFailFile 是否跳过删除失败的子项继续处理
     * @return 全部删除成功返回 {@code true}
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
     * 复制文件。
     *
     * <p>{@code dest} 为已存在目录时，复制到该目录下并沿用源文件名；否则作为
     * 目标文件路径。目标父目录会自动创建；源必须是普通文件（不支持目录复制），
     * 且目标不能与源相同或位于源路径之下。
     *
     * @param src 源文件路径，不能为空白
     * @param dest 目标文件路径或已存在目录，不能为空白
     * @param isOverride {@code true} 覆盖已存在目标；{@code false} 时目标存在抛出 {@link FileAlreadyExistsException}
     * @return 复制后的目标文件
     * @throws IOException 复制失败或目标已存在
     * @throws IllegalArgumentException 路径为空白
     * @throws RuntimeException 源不存在/不是文件，或目标与源相同/位于源之下
     */
    public static File copy(String src, String dest, boolean isOverride) throws IOException {
        AssertTool.notBlank(src, "source file path is blank");
        AssertTool.notBlank(dest, "destination file path is blank");

        return copy(Paths.get(src), Paths.get(dest), isOverride);
    }

    /**
     * 复制文件。
     *
     * <p>目标为已存在目录时复制到该目录内并沿用源文件名；目标父目录自动创建。
     * 仅支持复制普通文件，且目标不能与源相同或位于源路径之下。
     *
     * @param src 源文件，不能为 {@code null}
     * @param dest 目标文件或已存在目录，不能为 {@code null}
     * @param isOverride {@code true} 覆盖已存在目标；{@code false} 时目标存在抛出 {@link FileAlreadyExistsException}
     * @return 复制后的目标文件
     * @throws IOException 复制失败或目标已存在
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws RuntimeException 源不存在/不是文件，或目标与源相同/位于源之下
     */
    public static File copy(File src, File dest, boolean isOverride) throws IOException {
        AssertTool.notNull(src, "source file is null");
        AssertTool.notNull(dest, "destination file is null");

        return copy(src.toPath(), dest.toPath(), isOverride);
    }

    /**
     * 复制文件。
     *
     * <p>目标为已存在目录时复制到该目录内并沿用源文件名；目标父目录自动创建。
     * 仅支持复制普通文件，且目标不能与源相同或位于源路径之下。
     *
     * @param src 源文件路径，不能为 {@code null}
     * @param dest 目标路径或已存在目录，不能为 {@code null}
     * @param isOverride {@code true} 覆盖已存在目标；{@code false} 时目标存在抛出 {@link FileAlreadyExistsException}
     * @return 复制后的目标文件
     * @throws IOException 复制失败或目标已存在
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws RuntimeException 源不存在/不是文件，或目标与源相同/位于源之下
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
     * 按 {@link StandardCopyOption} 复制文件。
     *
     * <p>约束：源必须存在且为普通文件（目录复制暂不支持）；目标不能与源相同，
     * 也不能位于源路径之下。{@code dest} 为已存在目录时复制到该目录内并沿用
     * 源文件名；目标父目录自动创建。是否覆盖由 {@code options} 中的
     * {@link StandardCopyOption#REPLACE_EXISTING} 决定，未指定时目标已存在会抛出
     * {@link FileAlreadyExistsException}。
     *
     * @param src 源文件路径，不能为 {@code null}
     * @param dest 目标路径或已存在目录，不能为 {@code null}
     * @param options 复制选项，可为空数组
     * @return 复制后的目标文件
     * @throws IOException 复制失败或目标已存在
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws RuntimeException 源不存在/不是文件，或目标与源相同/位于源之下
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
     * 移动文件或目录。
     *
     * <p>等价于 {@code move(Paths.get(src), Paths.get(dest), isOverride)}。
     *
     * @param src 源路径，不能为空白
     * @param dest 目标路径或已存在目录，不能为空白
     * @param isOverride {@code true} 覆盖已存在目标；{@code false} 时目标存在抛出 {@link FileAlreadyExistsException}
     * @return 移动后的目标文件
     * @throws IOException 移动失败或目标已存在
     * @throws IllegalArgumentException 路径为空白
     * @throws RuntimeException 源不存在，或目标与源相同/位于源之下
     */
    public static File move(String src, String dest, boolean isOverride) throws IOException {
        AssertTool.notBlank(src, "source file path is blank");
        AssertTool.notBlank(dest, "destination file path is blank");

        return move(Paths.get(src), Paths.get(dest), isOverride);
    }

    /**
     * 移动文件或目录。
     *
     * <p>{@code dest} 为已存在目录时，移动到该目录下并沿用源名称；否则作为
     * 目标路径。目标父目录自动创建；目标不能与源相同或位于源路径之下。
     * 与复制不同，移动支持目录。
     *
     * @param src 源文件或目录，不能为 {@code null}
     * @param dest 目标文件/目录或已存在目录，不能为 {@code null}
     * @param isOverride {@code true} 覆盖已存在目标；{@code false} 时目标存在抛出 {@link FileAlreadyExistsException}
     * @return 移动后的目标文件
     * @throws IOException 移动失败或目标已存在
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws RuntimeException 源不存在，或目标与源相同/位于源之下
     */
    public static File move(File src, File dest, boolean isOverride) throws IOException {
        AssertTool.notNull(src, "source file is null");
        AssertTool.notNull(dest, "destination file is null");

        return move(src.toPath(), dest.toPath(), isOverride);
    }

    /**
     * 移动文件或目录。
     *
     * <p>目标为已存在目录时移动到该目录内并沿用源名称；目标父目录自动创建；
     * 目标不能与源相同或位于源路径之下。移动支持目录。
     *
     * @param src 源路径，不能为 {@code null}
     * @param dest 目标路径或已存在目录，不能为 {@code null}
     * @param isOverride {@code true} 覆盖已存在目标；{@code false} 时目标存在抛出 {@link FileAlreadyExistsException}
     * @return 移动后的目标文件
     * @throws IOException 移动失败或目标已存在
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws RuntimeException 源不存在，或目标与源相同/位于源之下
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
     * 按 {@link StandardCopyOption} 移动文件或目录。
     *
     * <p>底层为 {@link Files#move(Path, Path, CopyOption...)}：跨文件系统时可能
     * 退化为复制+删除，默认不保证原子性；需要原子性时传入
     * {@link StandardCopyOption#ATOMIC_MOVE}。目标父目录自动创建；是否覆盖由
     * {@code options} 决定。目标不能与源相同或位于源路径之下。
     *
     * @param src 源路径，不能为 {@code null}
     * @param dest 目标路径或已存在目录，不能为 {@code null}
     * @param options 移动选项，可为空数组
     * @return 移动后的目标文件
     * @throws IOException 移动失败或目标已存在
     * @throws IllegalArgumentException 任一参数为 {@code null}
     * @throws RuntimeException 源不存在，或目标与源相同/位于源之下
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
     * 重命名文件（同目录内移动）。
     *
     * <p>等价于把文件移动到同目录下的 {@code newFileName}；{@code newFileName}
     * 通常只包含文件名（传入带分隔符的相对路径时由
     * {@link Path#resolveSibling(String)} 解析）。是否覆盖由 {@code isOverride}
     * 决定，覆盖语义与 {@link #move(Path, Path, boolean)} 一致。
     *
     * @param file 原文件，不能为 {@code null}
     * @param newFileName 新文件名（不含目录），不能为空白
     * @param isOverride {@code true} 覆盖同名文件；{@code false} 时同名文件存在抛出 {@link FileAlreadyExistsException}
     * @return 重命名后的文件
     * @throws IOException 重命名失败或目标已存在
     * @throws IllegalArgumentException 文件为 {@code null} 或新文件名为空白
     */
    public static File rename(File file, String newFileName, boolean isOverride) throws IOException {
        AssertTool.notNull(file, "file path is null");
        AssertTool.notBlank(newFileName, "newFileName is blank");

        return move(file.toPath(), file.toPath().resolveSibling(newFileName), isOverride);
    }


    // ---------------------- write data ----------------------

    /**
     * 将字符串写入文件（UTF-8、覆盖已有内容）。
     *
     * <p>等价于 {@code writeString(path, content, false, StandardCharsets.UTF_8)}。
     * 文件不存在时自动创建（含父目录）；{@code content} 为 {@code null} 时不写入
     * 也不创建文件。
     *
     * @param path 文件路径，不能为空白
     * @param content 文件内容，可为 {@code null}
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白
     * @throws RuntimeException 路径存在但不是普通文件
     */
    public static void writeString(String path, String content) throws IOException {
        writeString(path, content, false, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串写入文件（UTF-8），可选择覆盖或追加。
     *
     * <p>文件不存在时自动创建（含父目录）；{@code content} 为 {@code null} 时
     * 直接返回，不创建文件。
     *
     * @param path 文件路径，不能为空白
     * @param content 文件内容，可为 {@code null}
     * @param append {@code true} 追加；{@code false} 覆盖已有内容
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白
     * @throws RuntimeException 路径存在但不是普通文件
     */
    public static void writeString(String path, String content, boolean append) throws IOException {
        writeString(path, content, append, StandardCharsets.UTF_8);
    }

    /**
     * 将字符串按指定字符集写入文件。
     *
     * <p>{@code append=false} 覆盖已有内容，{@code true} 追加；文件不存在时自动
     * 创建及其父目录。{@code content} 为 {@code null} 时直接返回；{@code charset}
     * 为 {@code null} 时按 UTF-8。整段字符串一次性交给缓冲 writer 写出，不额外
     * 追加换行符；writer 在方法内部 flush 并关闭。
     *
     * @param path 文件路径，不能为空白
     * @param content 文件内容，可为 {@code null}
     * @param append {@code true} 追加；{@code false} 覆盖
     * @param charset 字符集，可为 {@code null}（按 UTF-8）
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白
     * @throws RuntimeException 路径存在但不是普通文件
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
     * 将多行数据写入文件（UTF-8、追加模式、平台默认换行符）。
     *
     * <p>等价于 {@code writeLines(path, lines, null, true, null)}；非 {@code null}
     * 行元素通过 {@code toString()} 转字符串，{@code null} 元素写出空行。文件
     * 不存在时自动创建（含父目录）。
     *
     * @param path 文件路径，不能为空白
     * @param lines 行数据，可为 {@code null} 或空（此时不写入）
     * @throws IOException 写入失败
     */
    public static void writeLines(String path, Iterable<?> lines) throws IOException {
        writeLines(path, lines, null, true, null);
    }

    /**
     * 将多行数据写入文件（UTF-8、平台默认换行符）。
     *
     * <p>等价于 {@code writeLines(path, lines, null, append, null)}。
     *
     * @param path 文件路径，不能为空白
     * @param lines 行数据，可为 {@code null} 或空（此时不写入）
     * @param append {@code true} 追加；{@code false} 覆盖
     * @throws IOException 写入失败
     */
    public static void writeLines(String path, Iterable<?> lines, boolean append) throws IOException {
        writeLines(path, lines, null, append, null);
    }

    /**
     * 将多行数据按指定换行符、字符集写入文件。
     *
     * <p>逐行写出并 flush，内部缓冲 writer 在方法结束时关闭，适合较大数据量；
     * 非 {@code null} 行元素通过 {@code toString()} 转字符串，{@code null} 元素
     * 只写换行符。{@code lines} 为 {@code null} 或空时不创建、不修改文件；
     * {@code lineSeparator} 为 {@code null} 时使用 {@link System#lineSeparator()}；
     * {@code charset} 为 {@code null} 时使用 UTF-8。文件不存在时自动创建及其
     * 父目录；{@code append=false} 覆盖已有内容。
     *
     * @param path 文件路径，不能为空白
     * @param lines 行数据，可为 {@code null} 或空
     * @param lineSeparator 行分隔符，可为 {@code null}（平台默认）
     * @param append {@code true} 追加；{@code false} 覆盖
     * @param charset 字符集，可为 {@code null}（UTF-8）
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白
     * @throws RuntimeException 路径存在但不是普通文件
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
     * 以迭代方式流式写入多行数据（UTF-8、追加模式、平台默认换行符）。
     *
     * <p>等价于 {@code writeLines(path, lineSupplier, null, true, null)}。
     * {@code lineSupplier.get()} 返回 {@code null} 时结束；每次取一行立即写出，
     * 不把全部数据载入内存，适合大文件或数据库游标等场景。
     *
     * @param path 文件路径，不能为空白
     * @param lineSupplier 行数据提供者，不能为 {@code null}；返回 {@code null} 表示结束
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白或 provider 为 {@code null}
     */
    public static void writeLines(String path, Supplier<?> lineSupplier) throws IOException {
        writeLines(path, lineSupplier, null, true, null);
    }

    /**
     * 以迭代方式流式写入多行数据（UTF-8、平台默认换行符）。
     *
     * <p>等价于 {@code writeLines(path, lineSupplier, null, append, null)}。
     *
     * @param path 文件路径，不能为空白
     * @param lineSupplier 行数据提供者，不能为 {@code null}；返回 {@code null} 表示结束
     * @param append {@code true} 追加；{@code false} 覆盖
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白或 provider 为 {@code null}
     */
    public static void writeLines(String path, Supplier<?> lineSupplier, boolean append) throws IOException {
        writeLines(path, lineSupplier, null, append, null);
    }

    /**
     * 以迭代方式流式写入多行数据。
     *
     * <p>循环调用 {@code lineSupplier.get()}，每次返回一行（通过
     * {@link String#valueOf(Object)} 转字符串），返回 {@code null} 时结束；逐行
     * 写出并 flush，不缓存全量数据。文件不存在时先创建（含父目录），因此
     * supplier 立即返回 {@code null} 时会留下空文件。
     * {@code lineSeparator} 为 {@code null} 时使用 {@link System#lineSeparator()}，
     * {@code charset} 为 {@code null} 时使用 UTF-8；{@code append=false} 覆盖已有
     * 内容。
     *
     * @param path 文件路径，不能为空白
     * @param lineSupplier 行数据提供者，不能为 {@code null}；返回 {@code null} 表示结束
     * @param lineSeparator 行分隔符，可为 {@code null}（平台默认）
     * @param append {@code true} 追加；{@code false} 覆盖
     * @param charset 字符集，可为 {@code null}（UTF-8）
     * @throws IOException 写入失败
     * @throws IllegalArgumentException 路径为空白或 provider 为 {@code null}
     * @throws RuntimeException 路径存在但不是普通文件
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
     * 读取文件全部内容为字符串（UTF-8）。
     *
     * <p>等价于 {@code readString(path, StandardCharsets.UTF_8)}；文件不存在返回
     * {@code null}。内容一次性读入内存，大文件请使用
     * {@link #readLines(String, Consumer)} 流式读取。
     *
     * @param path 文件路径，不能为空白
     * @return 文件内容；文件不存在时返回 {@code null}
     * @throws IOException 读取失败
     * @throws IllegalArgumentException 路径为空白
     */
    public static String readString(String path) throws IOException {
        return readString(path, StandardCharsets.UTF_8);
    }

    /**
     * 按指定字符集读取文件全部内容为字符串。
     *
     * <p>文件不存在返回 {@code null}；{@code charset} 为 {@code null} 时按 UTF-8。
     * 内容一次性读入内存，换行符按文件原样保留（不做平台转换）。
     *
     * @param path 文件路径，不能为空白
     * @param charset 字符集，可为 {@code null}（UTF-8）
     * @return 文件内容；文件不存在时返回 {@code null}
     * @throws IOException 读取失败
     * @throws IllegalArgumentException 路径为空白
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
     * 读取文件全部行（UTF-8）。
     *
     * <p>等价于 {@code readLines(path, StandardCharsets.UTF_8)}；文件不存在返回
     * 空列表。结果一次性载入内存，行分隔符（CR/LF/CRLF）会被去除；大文件请
     * 使用 {@link #readLines(String, Consumer)}。
     *
     * @param path 文件路径，不能为空白
     * @return 行列表；文件不存在时返回空列表
     * @throws IOException 读取失败
     * @throws IllegalArgumentException 路径为空白
     */
    public static List<String> readLines(String path) throws IOException {
        return readLines(path, StandardCharsets.UTF_8);
    }

    /**
     * 按指定字符集读取文件全部行。
     *
     * <p>文件不存在返回空列表；{@code charset} 为 {@code null} 时按 UTF-8。
     * 结果一次性载入内存，行分隔符会被去除。
     *
     * @param path 文件路径，不能为空白
     * @param charset 字符集，可为 {@code null}（UTF-8）
     * @return 行列表；文件不存在时返回空列表
     * @throws IOException 读取失败
     * @throws IllegalArgumentException 路径为空白
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
     * 逐行读取文件并回调（UTF-8），适合大文件。
     *
     * <p>等价于 {@code readLines(path, StandardCharsets.UTF_8, lineConsumer)}；
     * 文件不存在时直接返回。读取过程中流式回调，不缓存全部行，内部 reader 在
     * 方法结束时关闭。
     *
     * @param path 文件路径，不能为空白
     * @param lineConsumer 行消费者，不能为 {@code null}
     * @throws IOException 读取失败
     * @throws IllegalArgumentException 路径为空白或 consumer 为 {@code null}
     */
    public static void readLines(String path, Consumer<String> lineConsumer) throws IOException {
        readLines(path, StandardCharsets.UTF_8, lineConsumer);
    }

    /**
     * 按指定字符集逐行读取文件并回调，适合大文件。
     *
     * <p>文件不存在时直接返回；{@code charset} 为 {@code null} 时按 UTF-8。
     * 每读到一行立即回调，不缓存全部行，内部 reader 在方法结束时关闭。
     *
     * @param path 文件路径，不能为空白
     * @param charset 字符集，可为 {@code null}（UTF-8）
     * @param lineConsumer 行消费者，不能为 {@code null}
     * @throws IOException 读取失败
     * @throws IllegalArgumentException 路径为空白或 consumer 为 {@code null}
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
