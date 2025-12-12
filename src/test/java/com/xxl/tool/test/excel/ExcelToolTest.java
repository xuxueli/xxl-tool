package com.xxl.tool.test.excel;


import com.xxl.tool.excel.ExcelTool;
import com.xxl.tool.io.FileTool;
import com.xxl.tool.json.GsonTool;
import com.xxl.tool.test.excel.model.ShopDTO;
import com.xxl.tool.test.excel.model.UserDTO;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

/**
 * FUN Test
 *
 * @author xuxueli 2017-09-08 22:41:19
 */
public class ExcelToolTest {
    private static final Logger logger = LoggerFactory.getLogger(ExcelToolTest.class);

    @Test
    public void test() {
        /**
         * Mock数据，Java对象列表
         */
        List<ShopDTO> shopDTOList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ShopDTO shop = new ShopDTO(true, "商户"+i, (short) i, 1000+i, 10000+i, (float) (1000+i), (double) (10000+i), new Date(), "备注" + i);
            shopDTOList.add(shop);
        }

        List<UserDTO> userDTOList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            userDTOList.add(new UserDTO(i, "用户"+i));
        }

        String filePath = "/Users/admin/Downloads/excel/demo-sheet.xlsx";  // excel 2007
        FileTool.delete(filePath);

        /**
         * Excel导出：Object 转换为 Excel
         */
        ExcelTool.writeFile(filePath, shopDTOList, userDTOList);

        /**
         * Excel导入：Excel 转换为 Object
         */
        List<ShopDTO> list2 = ExcelTool.readExcel(filePath, ShopDTO.class);
        logger.info(list2.size() + ":" + list2);

        List<UserDTO> list3 = ExcelTool.readExcel(filePath, UserDTO.class);
        logger.info(list3.size() + ":" + list3);

    }

    @Test
    public void readExcel_default() {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            userDTOList.add(new UserDTO(i, "用户"+i));
        }

        String filePath = "/Users/admin/Downloads/excel/demo-sheet-02.xlsx";  // excel 2007
        FileTool.delete(filePath);
        /**
         * Excel导出：Object 转换为 Excel
         */
        ExcelTool.writeFile(filePath, userDTOList);

        /**
         * Excel导入：Excel 转换为 Object
         */
        List<UserDTO> list3 = ExcelTool.readExcel(filePath, UserDTO.class);
        logger.info(list3.size() + ":" + list3);
    }

    @Test
    public void readExcel_Stream() {
        List<UserDTO> userDTOList = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            userDTOList.add(new UserDTO(i, "用户"+i));
        }

        String filePath = "/Users/admin/Downloads/excel/demo-sheet-02.xlsx";  // excel 2007
        FileTool.delete(filePath);
        /**
         * Excel导出：Object 转换为 Excel
         */
        ExcelTool.writeFile(filePath, userDTOList);

        /**
         * Excel导入：Excel 转换为 Object
         */
        ExcelTool.readExcel(filePath, new Consumer<UserDTO>() {
            @Override
            public void accept(UserDTO userDTO) {
                logger.info("item: " + GsonTool.toJson(userDTO));
            }
        });
    }

}
