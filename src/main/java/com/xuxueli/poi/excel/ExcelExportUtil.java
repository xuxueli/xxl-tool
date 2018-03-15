package com.xuxueli.poi.excel;

import com.xuxueli.poi.excel.annotation.ExcelField;
import com.xuxueli.poi.excel.annotation.ExcelSheet;
import com.xuxueli.poi.excel.util.FieldReflectionUtil;
import com.xuxueli.poi.excel.util.RandomStrUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * EXCEL Export工具
 *
 * @author wj42134
 * @date 2017 12 22 00:09
 */
public class ExcelExportUtil {
    /**
     * The constant logger.
     */
    private static Logger logger = LoggerFactory.getLogger(ExcelExportUtil.class);


    /**
     * 导出Excel对象
     *
     * @param sheetDataListArr Excel数据
     * @return workbook
     */
    private static <T> SXSSFWorkbook exportWorkbook(List<T>... sheetDataListArr) {

        // data array valid
        if (sheetDataListArr == null || sheetDataListArr.length == 0) {
            throw new RuntimeException(">>>>>>>>>>> excel error, data array can not be empty.");
        }

        // book （HSSFWorkbook=2003/xls、XSSFWorkbook=2007/xlsx SXSSFWorkbook for Large Excel）
        SXSSFWorkbook workbook = new SXSSFWorkbook(500);
        workbook.setCompressTempFiles(true);

        // sheet
        for (List<T> dataList : sheetDataListArr) {
            makeSheet(workbook, dataList);
        }

        return workbook;
    }

    /**
     * Make sheet.
     *
     * @param workbook      the workbook
     * @param sheetDataList the sheet data list
     */
    private static <T> void makeSheet(SXSSFWorkbook workbook, List<T> sheetDataList) {
        // data
        if (sheetDataList == null || sheetDataList.size() == 0) {
            throw new RuntimeException(">>>>>>>>>>> excel error, data can not be empty.");
        }

        // sheet
        Class<?> sheetClass = sheetDataList.get(0).getClass();
        ExcelSheet excelSheet = sheetClass.getAnnotation(ExcelSheet.class);

        String sheetName = sheetDataList.get(0).getClass().getSimpleName();
        int headColorIndex = -1;
        if (excelSheet != null) {
            if (StringUtils.isNotBlank(excelSheet.name().trim())) {
                sheetName = excelSheet.name().trim();
            }
            headColorIndex = excelSheet.headColor().getIndex();
        }

        Sheet existSheet = workbook.getSheet(sheetName);
        if (existSheet != null) {
            // avoid sheetName repetition
            while (true) {
                String newSheetName = sheetName.concat(RandomStrUtils.getRandomStr(6));
                existSheet = workbook.getSheet(newSheetName);
                if (existSheet == null) {
                    sheetName = newSheetName;
                    break;
                }
            }
        }

        SXSSFSheet sheet = workbook.createSheet(sheetName);

        // sheet field
        List<Field> fields = new ArrayList<>();
        for (; sheetClass != Object.class; sheetClass = sheetClass.getSuperclass()) {
            if (sheetClass.getDeclaredFields() != null && sheetClass.getDeclaredFields().length > 0) {
                for (Field field : sheetClass.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    // only read contain ExcelField Annotation
                    if (field.getAnnotation(ExcelField.class) != null) {
                        fields.add(field);
                    }
                }
            }
        }

        if (fields == null || fields.size() == 0) {
            throw new RuntimeException(">>>>>>>>>>> excel error, data field can not be empty.");
        }

        // sheet header row
        CellStyle[] fieldDataStyleArr = new CellStyle[fields.size()];
        int[] fieldWidthArr = new int[fields.size()];
        Row headRow = sheet.createRow(0);
        for (int i = 0; i < fields.size(); i++) {

            // field
            Field field = fields.get(i);
            ExcelField excelField = field.getAnnotation(ExcelField.class);

            String fieldName = field.getName();
            int fieldWidth = 0;
            HorizontalAlignment align = null;
            if (excelField != null) {
                if (excelField.name() != null && excelField.name().trim().length() > 0) {
                    fieldName = excelField.name().trim();
                }
                fieldWidth = excelField.width();
                align = excelField.align();
            }

            // field width
            fieldWidthArr[i] = fieldWidth;

            // head-style、field-data-style
            CellStyle fieldDataStyle = workbook.createCellStyle();
            if (align != null) {
                fieldDataStyle.setAlignment(align);
            }
            fieldDataStyleArr[i] = fieldDataStyle;

            CellStyle headStyle = workbook.createCellStyle();
            headStyle.cloneStyleFrom(fieldDataStyle);
            if (headColorIndex > -1) {
                headStyle.setFillForegroundColor((short) headColorIndex);
                headStyle.setFillBackgroundColor((short) headColorIndex);
                headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            // head-field data
            Cell cellX = headRow.createCell(i, CellType.STRING);
            cellX.setCellStyle(headStyle);
            cellX.setCellValue(String.valueOf(fieldName));
        }

        // sheet data rows
        for (int dataIndex = 0; dataIndex < sheetDataList.size(); dataIndex++) {
            int rowIndex = dataIndex + 1;
            Object rowData = sheetDataList.get(dataIndex);

            Row rowX = sheet.createRow(rowIndex);

            for (int i = 0; i < fields.size(); i++) {
                Field field = fields.get(i);
                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(rowData);

                    String fieldValueString = FieldReflectionUtil.formatValue(field, fieldValue);

                    Cell cellX = rowX.createCell(i, CellType.STRING);
                    cellX.setCellValue(fieldValueString);
                    cellX.setCellStyle(fieldDataStyleArr[i]);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            }
        }

        // sheet finally
        for (int i = 0; i < fields.size(); i++) {
            int fieldWidth = fieldWidthArr[i];
            if (fieldWidth > 0) {
                sheet.setColumnWidth(i, fieldWidth);
            } else {
                sheet.trackColumnForAutoSizing(i); // 坑
                sheet.autoSizeColumn((short) i);
            }
        }
    }

    /**
     * 导出Excel文件到磁盘
     *
     * @param filePath         the file path
     * @param sheetDataListArr 数据，可变参数，如多个参数则代表导出多张Sheet
     */
    @SafeVarargs
    public static <T> void exportToFile(String filePath, List<T>... sheetDataListArr) {
        // workbook
        SXSSFWorkbook workbook = exportWorkbook(sheetDataListArr);

        FileOutputStream fileOutputStream = null;
        try {
            // workbook 2 FileOutputStream
            fileOutputStream = new FileOutputStream(filePath);
            workbook.write(fileOutputStream);

            // flush
            fileOutputStream.flush();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            execCloseStream(fileOutputStream, workbook);
        }
    }

    /**
     * 导出Excel字节数据
     *
     * @param sheetDataListArr the sheet data list arr
     * @return byte [ ]
     */
    @SafeVarargs
    public static <T> byte[] exportToBytes(List<T>... sheetDataListArr) {
        // workbook
        SXSSFWorkbook workbook = exportWorkbook(sheetDataListArr);

        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] result = null;
        try {
            // workbook 2 ByteArrayOutputStream
            byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);

            // flush
            byteArrayOutputStream.flush();

            result = byteArrayOutputStream.toByteArray();
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            execCloseStream(byteArrayOutputStream, workbook);
        }
    }

    private static void execCloseStream(OutputStream outputStream, SXSSFWorkbook workbook) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (workbook != null) {
                // 清除临时文件
                workbook.dispose();
                workbook.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

}
