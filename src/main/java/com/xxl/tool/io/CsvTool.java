package com.xxl.tool.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV 工具
 *
 * <p>提供按行读写 CSV 文件的能力，默认分隔符为英文逗号 {@code ','}、引号
 * 为英文双引号 {@code '"'}，也可通过重载自定义：
 * <ul>
 *     <li>写入：字段包含分隔符、引号或换行符（{@code "\n"}）时自动加引号，
 *     字段内部的引号按 CSV 规范双写转义；</li>
 *     <li>读取：支持引号包裹的字段与双写引号转义；按
 *     {@link BufferedReader#readLine()} 逐行解析，因此不支持跨多行的字段；</li>
 *     <li>不识别表头：第一行与普通数据行同等对待，是否包含表头由调用方约定；</li>
 *     <li>字符集使用平台默认字符集（{@link FileReader}/{@link FileWriter}），
 *     需要 UTF-8 等指定编码时不应使用本工具；</li>
 *     <li>写入会覆盖同名文件、不自动创建父目录，末尾统一追加换行；读取会一次性
 *     返回全部行（非流式）；流由方法内部 try-with-resources 关闭。</li>
 * </ul>
 *
 * <p>无状态工具类，方法可并发调用；I/O 异常统一包装为 {@link RuntimeException}。
 *
 * @author xuxueli 2025-05-17
 */
public class CsvTool {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    // --------------------------------- csv line parse/format ---------------------------------

    /**
     * 解析单行 CSV：按分隔符切分，引号内的分隔符不切分，连续两个引号转义为一个
     * 引号；不处理跨行字段。
     */
    private static String[] parseLine(String line, char separator, char quote) {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == quote) {
                // 处理引号
                if (inQuotes) {
                    // 检查是否是转义的引号 (两个连续引号)
                    if (i + 1 < line.length() && line.charAt(i + 1) == quote) {
                        field.append(quote);
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    inQuotes = true;
                }
            } else if (c == separator && !inQuotes) {
                // 找到分隔符且不在引号内，添加字段到结果
                result.add(field.toString());
                field.setLength(0);
            } else {
                // 普通字符，添加到当前字段
                field.append(c);
            }
        }

        // 添加最后一个字段
        result.add(field.toString());

        return result.toArray(new String[0]);
    }

    /**
     * 格式化单行 CSV：字段含分隔符、引号或换行时加引号，内部引号双写转义；
     * 不追加行尾换行符。
     */
    private static String formatLine(String[] values, char separator, char quote) {
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            String value = values[i];
            boolean needQuotes = value.contains(String.valueOf(separator)) ||
                    value.contains(String.valueOf(quote)) ||
                    value.contains("\n");

            if (needQuotes) {
                // 需要引号，处理内部引号
                line.append(quote)
                        .append(value.replace(String.valueOf(quote), String.valueOf(quote) + quote))
                        .append(quote);
            } else {
                line.append(value);
            }

            // 添加分隔符（最后一个字段除外）
            if (i < values.length - 1) {
                line.append(separator);
            }
        }

        return line.toString();
    }


    // --------------------------------- csv read/write ---------------------------------

    /**
     * 写入 CSV 文件（默认分隔符 {@code ','}、引号 {@code '"'}）。
     *
     * <p>等价于 {@code writeCsv(filePath, data, ',', '"')}。
     *
     * @param filePath 目标文件路径
     * @param data 数据行，每行为一个字符串数组
     */
    public static void writeCsv(String filePath, List<String[]> data) {
        writeCsv(filePath, data, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * 按指定分隔符与引号写入 CSV 文件。
     *
     * <p>使用平台默认字符集；同名文件会被覆盖，父目录不会自动创建。每行通过
     * {@link BufferedWriter#newLine()} 追加平台换行符（最后一行同样追加），
     * 字段按需加引号并转义内部引号。{@code data} 为 {@code null} 或行内元素为
     * {@code null} 时抛出 {@link NullPointerException}。
     *
     * @param filePath 目标文件路径
     * @param data 数据行，每行为一个字符串数组
     * @param separator 字段分隔符，如 {@code ','}、{@code ';'}
     * @param quote 引号字符，如 {@code '"'}
     * @throws RuntimeException 写入失败（I/O 异常被包装）
     */
    public static void writeCsv(String filePath, List<String[]> data, char separator, char quote)  {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            for (String[] row : data) {
                bw.write(formatLine(row, separator, quote));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("CsvTool writeCsv error.", e);
        }
    }

    /**
     * 读取 CSV 文件（默认分隔符 {@code ','}、引号 {@code '"'}）。
     *
     * <p>等价于 {@code readCsv(filePath, ',', '"')}。
     *
     * @param filePath CSV 文件路径
     * @return 全部行，每行为一个字符串数组（含可能的表头行）
     */
    public static List<String[]> readCsv(String filePath) {
        return readCsv(filePath, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * 按指定分隔符与引号读取 CSV 文件。
     *
     * <p>使用平台默认字符集；按 {@link BufferedReader#readLine()} 逐行解析并
     * 返回全部行（第一行不做表头特殊处理），空行会解析为仅含一个空字符串的
     * 数组。引号内的分隔符不切分，连续两个引号转义为一个引号；不支持跨行的
     * 多行字段。文件不存在或读取失败时抛出 {@link RuntimeException}。
     *
     * @param filePath CSV 文件路径
     * @param separator 字段分隔符
     * @param quote 引号字符
     * @return 全部行，每行为一个字符串数组
     * @throws RuntimeException 文件不存在或读取失败
     */
    public static List<String[]> readCsv(String filePath, char separator, char quote)  {
        List<String[]> result = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = parseLine(line, separator, quote);
                result.add(values);
            }
        } catch (IOException e) {
            throw new RuntimeException("CsvTool readCsv error.", e);
        }

        return result;
    }

}