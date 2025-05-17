package com.xxl.tool.io;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Tool
 *
 * @author xuxueli 2025-05-17
 */
public class CsvTool {

    private static final char DEFAULT_SEPARATOR = ',';
    private static final char DEFAULT_QUOTE = '"';

    // --------------------------------- csv line parse/format ---------------------------------

    /**
     * parse CSV line
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
     * format CSV line
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
     * write CSV file
     *
     * @param filePath destination file path
     * @param data     要写入的数据，每行是一个字符串数组
     */
    public static void writeCsv(String filePath, List<String[]> data) {
        writeCsv(filePath, data, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * write CSV file
     *
     * @param filePath  destination file path
     * @param data      要写入的数据，每行是一个字符串数组
     * @param separator 分隔符
     * @param quote     引号字符
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
     * read CSV file
     *
     * @param   filePath CSV file path
     * @return  包含所有行数据的列表，每行是一个字符串数组
     */
    public static List<String[]> readCsv(String filePath) {
        return readCsv(filePath, DEFAULT_SEPARATOR, DEFAULT_QUOTE);
    }

    /**
     * read CSV file
     *
     * @param filePath  CSV file path
     * @param separator 分隔符
     * @param quote     引号字符
     * @return          包含所有行数据的列表，每行是一个字符串数组
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