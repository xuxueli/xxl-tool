package com.xuxueli.poi.excel;

import com.xuxueli.poi.excel.annotation.ExcelField;
import com.xuxueli.poi.excel.annotation.ExcelSheet;
import com.xuxueli.poi.excel.util.FieldReflectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * EXCEL 导入工具
 *
 * @author wj42134
 * @date 2017 12 22 00:10
 */
public class ExcelImportUtil {
    /**
     * The constant logger.
     */
    private static Logger logger = LoggerFactory.getLogger(ExcelImportUtil.class);

    /**
     * 从Workbook导入Excel文件，并封装成对象
     *
     * @param workbook   the workbook
     * @param sheetClass the sheet class
     * @return list
     */
    private static <T> List<T> importExcel(Workbook workbook, Class<T> sheetClass) {
        return importSheet(workbook, sheetClass);
    }

    /**
     * Import sheet list.
     *
     * @param workbook   the workbook
     * @param sheetClass the sheet class
     * @return the list
     */
    private static <T> List<T> importSheet(Workbook workbook, Class<T> sheetClass) {
        try {
            // sheet
            ExcelSheet excelSheet = sheetClass.getAnnotation(ExcelSheet.class);
            String sheetName = excelSheet != null && excelSheet.name().trim().length() > 0 ?
                    excelSheet.name().trim() : sheetClass.getSimpleName();
            // sheet field
            Map<String, Field> fieldMap = new HashMap<>(); //key:ExcelField name, val:name

            Class tempClass = sheetClass;
            for (; tempClass != Object.class; tempClass = tempClass.getSuperclass()) {
                if (tempClass.getDeclaredFields() != null && tempClass.getDeclaredFields().length > 0) {
                    for (Field field : tempClass.getDeclaredFields()) {
                        if (Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        if (field.getAnnotation(ExcelField.class) != null) {
                            String name = field.getAnnotation(ExcelField.class).name();
                            fieldMap.put(name, field);
                        }
                    }
                }
            }

            int fieldSize = fieldMap.entrySet().size();
            if (fieldSize == 0) {
                throw new RuntimeException(">>>>>>>>>>> excel error, data field can not be empty.");
            }

            // sheet data
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return null;
            }

            Iterator<Row> sheetIterator = sheet.rowIterator();
            int rowIndex = 0;
            //get head name to arr
            Row headRow = sheet.getRow(0);
            int colLength = headRow.getPhysicalNumberOfCells();
            //key:colName, value:col index
            String[] sheetHeaders = new String[colLength];
            for (int i = 0; i < colLength; i++) {
                String cellValue = headRow.getCell(i).getStringCellValue();
                if (StringUtils.isNotBlank(cellValue)) {
                    cellValue.trim();
                }
                sheetHeaders[i] = cellValue;
            }
            List<T> dataList = new ArrayList<>();
            int countField = 0;
            while (sheetIterator.hasNext()) {
                Row rowX = sheetIterator.next();
                if (rowIndex > 0) {
                    // 类必须有空参数构造方法
                    T rowObj = sheetClass.getDeclaredConstructor().newInstance();
                    //循环excel列属性
                    for (int i = 0; i < sheetHeaders.length; i++) {
                        //excel col name
                        String fname = sheetHeaders[i];
                        //col name empty ,don't save
                        if (StringUtils.isBlank(fname)) {
                            continue;
                        }
                        // cell(col i)
                        Cell cell = rowX.getCell(i);
                        if (cell == null) {
                            continue;
                        }

                        // call val str
                        cell.setCellType(CellType.STRING);
                        // cell.getCellTypeEnum()
                        String fieldValueStr = cell.getStringCellValue();

                        // java val
                        Field field = fieldMap.get(fname);
                        //annotated field not exist,skip(error)
                        if (field == null) {
                            throw new RuntimeException(">>>>>>>>>>> excel error, check is your col name matches this bean excel field you defined。" + fname);
                        }
                        Object fieldValue = FieldReflectionUtil.parseValue(field, fieldValueStr);
                        if (fieldValue == null) {
                            // 计数 空字段数
                            countField++;
                            continue;
                        }
                        // fill val
                        field.setAccessible(true);
                        field.set(rowObj, fieldValue);
                    }

                    if (countField != sheetHeaders.length) {
                        dataList.add(rowObj);
                    }
                }
                rowIndex++;
                // 空字段数 清空
                countField = 0;
            }
            return dataList;
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件路径导入Excel，并封装成对象
     *
     * @param filePath   the file path
     * @param sheetClass the sheet class
     * @return list
     */
    public static <T> List<T> importExcel(String filePath, Class<T> sheetClass) throws IOException {
        File excelFile = new File(filePath);
        return importExcel(excelFile, sheetClass);
    }

    /**
     * 文件导入Excel，并封装成对象
     *
     * @param excelFile  the excel file
     * @param sheetClass the sheet class
     * @return list
     */
    public static <T> List<T> importExcel(File excelFile, Class<T> sheetClass) throws IOException {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(excelFile);
            return importExcel(workbook, sheetClass);
        } catch (IOException | InvalidFormatException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

    /**
     * 数据流导入Excel，并封装成对象
     *
     * @param inputStream the input stream
     * @param sheetClass  the sheet class
     * @return list
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> sheetClass) throws IOException {
        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(inputStream);
            return importExcel(workbook, sheetClass);
        } catch (IOException | InvalidFormatException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (workbook != null) {
                workbook.close();
            }
        }
    }

}
