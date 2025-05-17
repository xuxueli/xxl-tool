package com.xxl.tool.test.excel;


import com.xxl.tool.excel.ExcelTool;
import com.xxl.tool.test.excel.model.ShopDTO;
import com.xxl.tool.test.excel.model.UserDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * FUN Test
 *
 * @author xuxueli 2017-09-08 22:41:19
 */
public class ExcelToolTest {

    public static void main(String[] args) {

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

        String filePath = "/Users/admin/Downloads/demo-sheet.xlsx";  // excel 2007

        /**
         * Excel导出：Object 转换为 Excel
         */
        ExcelTool.writeFile(filePath, shopDTOList, userDTOList);

        /**
         * Excel导入：Excel 转换为 Object
         */
        List<ShopDTO> list2 = ExcelTool.readExcel(filePath, ShopDTO.class);
        System.out.println(list2.size() + ":" + list2);

        List<UserDTO> list3 = ExcelTool.readExcel(filePath, UserDTO.class);
        System.out.println(list3.size() + ":" + list3);

    }

}
