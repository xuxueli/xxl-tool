package com.xxl.tool.test.excel;


import com.xxl.tool.excel.ExcelTool;
import com.xxl.tool.test.excel.model.ShopDTO;

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
        List<ShopDTO> shopDTOList = new ArrayList<ShopDTO>();
        for (int i = 0; i < 100; i++) {
            ShopDTO shop = new ShopDTO(true, "商户"+i, (short) i, 1000+i, 10000+i, (float) (1000+i), (double) (10000+i), new Date());
            shopDTOList.add(shop);
        }
        String filePath = "/Users/xuxueli/Downloads/demo-sheet.xls";    // excel 2003
        String filePath2 = "/Users/xuxueli/Downloads/demo-sheet.xlsx";  // excel 2007

        /**
         * Excel导出：Object 转换为 Excel
         */
        ExcelTool.exportToFile(false, Arrays.asList(shopDTOList), filePath);
        ExcelTool.exportToFile(Arrays.asList(shopDTOList), filePath2);

        /**
         * Excel导入：Excel 转换为 Object
          */
        List<Object> list = ExcelTool.importExcel(filePath, ShopDTO.class);
        List<Object> list2 = ExcelTool.importExcel(filePath2, ShopDTO.class);

        System.out.println(list);
        System.out.println(list2);

    }

}
