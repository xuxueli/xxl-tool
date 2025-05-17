package com.xxl.tool.test.io;

import com.xxl.tool.io.CsvTool;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CsvToolTest {
    private static final Logger logger = LoggerFactory.getLogger(CsvToolTest.class);

    @Test
    public void test() {

        // 创建示例数据
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"用户ID", "用户名", "年龄", "城市", "是否VIP", "新增时间", "别名", "访问量"});
        data.add(new String[]{"1", "张三", "25", "北京", "true", "2023-11-11 11:11:11", "Jack", "100"});
        data.add(new String[]{"2", "李四", "30", "上海", "false", "2023-11-11 11:11:11", "Rose", "200"});
        data.add(new String[]{"3", "王五,测试", "28", "广州", "true", "2023-11-11 11:11:11", "Rose", "200"});  // 包含逗号，会自动加引号

        for (int i = 0; i < 10000; i++) {
            data.add(new String[]{"" + i, "用户" + i, "20", "北京", "true", "2023-11-11 11:11:11", "别名" + i, "100"});
        }

        // 写入CSV
        String filePath = "/Users/admin/Downloads/demo-csv.csv";
        CsvTool.writeCsv(filePath, data);
        logger.info("CSV文件已写入: " + filePath);

        // 读取CSV
        List<String[]> readData = CsvTool.readCsv(filePath);
        System.out.println("读取的CSV数据:");
        for (String[] row : readData) {
            logger.info(String.join(" | ", row));
        }

        /**
         * // 读取CSV文件
         * List<String[]> data = CsvUtils.readCsv("input.csv");
         *
         * // 写入CSV文件
         * CsvUtils.writeCsv("output.csv", data);
         *
         * // 使用自定义分隔符（如分号）
         * List<String[]> data = CsvUtils.readCsv("input.csv", ';', '"');
         * CsvUtils.writeCsv("output.csv", data, ';', '"');
         */

    }
}
