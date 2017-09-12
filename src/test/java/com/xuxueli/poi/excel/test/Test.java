package com.xuxueli.poi.excel.test;

import com.xuxueli.poi.excel.ExcelExportUtil;
import com.xuxueli.poi.excel.ExcelImportUtil;
import com.xuxueli.poi.excel.test.model.ShopDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * FUN Test
 *
 * @author xuxueli 2017-09-08 22:41:19
 */
public class Test {

    public static void main(String[] args) {

        /**
         * Mock数据，Java对象列表
         */
        List<ShopDTO> shopDTOList = new ArrayList<ShopDTO>();
        for (int i = 0; i < 100; i++) {
            ShopDTO shop = new ShopDTO(i, "商户"+i);
            shopDTOList.add(shop);
        }

        /**
         * Excel导出：Object 转换为 Excel
         */
        String filePath = "/Users/xuxueli/Downloads/demo-sheet.xls";
        ExcelExportUtil.exportToFile(shopDTOList, filePath);

        /**
         * Excel导入：Excel 转换为 Object
          */
        List<Object> list = ExcelImportUtil.importExcel(ShopDTO.class, filePath);
        System.out.println(list);

    }

}
