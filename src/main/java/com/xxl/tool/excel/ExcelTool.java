package com.xxl.tool.excel;

import com.xxl.tool.excel.annotation.ExcelField;
import com.xxl.tool.excel.annotation.ExcelSheet;
import com.xxl.tool.excel.util.FieldReflectionUtil;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.*;

/**
 * Excel导入/导出工具
 *
 * A flexible tool for translating Java objects and Excel documents.
 *
 * @author xuxueli 2017-09-08 22:27:20
 */
public class ExcelTool {
    private static final Logger logger = LoggerFactory.getLogger(ExcelTool.class);


    // ---------------------- write Workbook ----------------------

    /**
     * create Workbook
     */
    private static Workbook createWorkbook(List<?>... sheetDataList){

        // valid sheet-list data
        if (sheetDataList==null || sheetDataList.length==0) {
            throw new RuntimeException("ExcelTool createWorkbook error, sheetData can not be empty.");
        }

        // init Workbook
        Workbook workbook = new XSSFWorkbook();

        // write sheet-data
        for (List<?> sheetData: sheetDataList) {
            createSheet(workbook, sheetData);
        }

        return workbook;
    }

    /**
     * create Sheet
     */
    private static void createSheet(Workbook workbook, List<?> sheetData){

        // valid data
        if (sheetData==null || sheetData.isEmpty()) {
            return;
            //throw new RuntimeException("ExcelTool createSheet error, sheetData can not be empty.");
        }

        // parse sheet-class
        Class<?> sheetClass = sheetData.get(0).getClass();
        ExcelSheet excelSheetAnno = sheetClass.getAnnotation(ExcelSheet.class);

        // parse sheet-config
        String sheetName = sheetClass.getSimpleName();
        short headColorIndex = -1;
        if (excelSheetAnno != null) {
            if (excelSheetAnno.name()!=null && !excelSheetAnno.name().trim().isEmpty()) {
                sheetName = excelSheetAnno.name().trim();
            }
            headColorIndex = excelSheetAnno.headColor().getIndex();
        }

        // parse sheet-field
        List<Field> fields = new ArrayList<>();
        for (Field field : sheetClass.getDeclaredFields()) {
            // ignore static field
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            // ignore ignore-field
            ExcelField excelFieldAnno = field.getAnnotation(ExcelField.class);
            if (excelFieldAnno != null && excelFieldAnno.ignore()) {
                continue;
            }
            fields.add(field);
        }
        if (fields.isEmpty()) {
            throw new RuntimeException("ExcelTool createSheet error, sheetClass fields can not be empty.");
        }

        // create Sheet
        Sheet existSheet = workbook.getSheet(sheetName);
        if (existSheet != null) {
            for (int i = 2; i <= 1000; i++) {
                String newSheetName = sheetName.concat(String.valueOf(i));  // avoid sheetName repetition
                existSheet = workbook.getSheet(newSheetName);
                if (existSheet == null) {
                    sheetName = newSheetName;
                    break;
                }
            }
        }
        Sheet sheet = workbook.createSheet(sheetName);

        // write sheet-header row
        CellStyle[] fieldDataStyleArr = new CellStyle[fields.size()];
        int[] fieldWidthArr = new int[fields.size()];
        Row headRow = sheet.createRow(0);
        for (int i = 0; i < fields.size(); i++) {

            // parse field-anno
            Field field = fields.get(i);
            ExcelField excelFieldAnno = field.getAnnotation(ExcelField.class);

            // parse field-config
            String fieldName = field.getName();
            int fieldWidth = 0;
            HorizontalAlignment align = null;
            if (excelFieldAnno != null) {
                if (excelFieldAnno.name()!=null && !excelFieldAnno.name().trim().isEmpty()) {
                    fieldName = excelFieldAnno.name().trim();
                }
                fieldWidth = excelFieldAnno.width();
                align = excelFieldAnno.align();
            }

            // field-width
            fieldWidthArr[i] = fieldWidth;

            // field-data-style (+align)
            CellStyle fieldDataStyle = workbook.createCellStyle();
            if (align != null) {
                fieldDataStyle.setAlignment(align);
            }
            fieldDataStyleArr[i] = fieldDataStyle;

            // head-style ( +align +color)
            CellStyle headStyle = workbook.createCellStyle();
            headStyle.cloneStyleFrom(fieldDataStyle);
            if (headColorIndex > -1) {
                headStyle.setFillForegroundColor(headColorIndex);
                headStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            }

            // write head-row
            Cell cellX = headRow.createCell(i, CellType.STRING);
            cellX.setCellStyle(headStyle);
            cellX.setCellValue(fieldName);
        }

        // write sheet-data rows
        writeRowData(sheet, fields, fieldDataStyleArr, sheetData);

        // write sheet-width
        writeColumnWidth(sheet, fields, fieldWidthArr);
    }

    /**
     * write List<Object> 2 sheet-data
     */
    private static void writeRowData(Sheet sheet, List<Field> fields, CellStyle[] fieldDataStyleArr, List<?> sheetData){
        for (int dataIndex = 0; dataIndex < sheetData.size(); dataIndex++) {

            // prepare row + row-data
            Object rowData = sheetData.get(dataIndex);
            Row rowX = sheet.createRow(dataIndex + 1);      // skip head-row

            // write row-data
            for (int i = 0; i < fields.size(); i++) {
                try {
                    // parse field-value
                    Field field = fields.get(i);
                    field.setAccessible(true);
                    Object fieldValue = field.get(rowData);

                    // convert to string
                    String fieldValueString = FieldReflectionUtil.formatValue(field, fieldValue);

                    // write row-data
                    Cell cellX = rowX.createCell(i, CellType.STRING);       //rowX.createCell(i);
                    cellX.setCellStyle(fieldDataStyleArr[i]);
                    cellX.setCellValue(fieldValueString);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("ExcelTool createSheet error, write row-data error.", e);
                }
            }
        }
    }

    /**
     * wtite column width
     */
    private static void writeColumnWidth(Sheet sheet, List<Field> fields, int[] fieldWidthArr) {
        for (int i = 0; i < fields.size(); i++) {
            int fieldWidth = fieldWidthArr[i];
            if (fieldWidth > 0) {
                sheet.setColumnWidth(i, fieldWidth);
            } else {
                sheet.autoSizeColumn((short)i);
            }
        }
    }


    // ---------------------- read Workbook ----------------------

    /**
     * date formatter
     */
    private static final DataFormatter FORMATTER = new DataFormatter();

    /**
     * read sheet-data 2 List<Object>
     */
    private static List<Object> readSheet(Workbook workbook, Class<?> sheetClass) {
        try {
            // parse sheet config
            ExcelSheet excelSheet = sheetClass.getAnnotation(ExcelSheet.class);
            String sheetName = (excelSheet!=null && excelSheet.name()!=null && !excelSheet.name().trim().isEmpty())
                    ?excelSheet.name().trim()
                    :sheetClass.getSimpleName();

            // parse sheet-field
            List<Field> fields = new ArrayList<>();
            for (Field field : sheetClass.getDeclaredFields()) {
                // ignore static-field
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                // ignore ignore-field
                ExcelField excelFieldAnno = field.getAnnotation(ExcelField.class);
                if (excelFieldAnno != null && excelFieldAnno.ignore()) {
                    continue;
                }
                fields.add(field);
            }
            if (fields.isEmpty()) {
                throw new RuntimeException("ExcelTool readSheet error, sheetClass[" + sheetClass.getName() + "] fields can not be empty.");
            }

            // load sheet
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return null;
            }

            // parse fieldName 2 index, from head-row
            Row headRow = sheet.getRow(0);
            if (headRow == null) {
                return null;
            }
            Map<Integer, String> cellIndex2fieldName = new HashMap<>();
            for (int i = 0; i < headRow.getLastCellNum(); i++) {
                Cell cell = headRow.getCell(i);
                if (cell == null) {
                    continue;
                }
                String cellValueStr = FORMATTER.formatCellValue(cell);
                cellIndex2fieldName.put(i, cellValueStr);
            }

            // parse field 2 fiendName
            Map<String, Field> fieldMame2FieldMap = new HashMap<>();
            for (Field field : fields) {
                String fieldName = field.getName();
                ExcelField excelFieldAnno = field.getAnnotation(ExcelField.class);
                if (excelFieldAnno != null && excelFieldAnno.name() != null && !excelFieldAnno.name().trim().isEmpty()) {
                    fieldName = excelFieldAnno.name().trim();
                }
                fieldMame2FieldMap.put(fieldName, field);
            }

            // parse sheet-data
            List<Object> sheetData = new ArrayList<>();
            Iterator<Row> sheetIterator = sheet.rowIterator();
            int rowIndex = 0;
            while (sheetIterator.hasNext()) {
                Row rowX = sheetIterator.next();
                if (rowIndex > 0) {     // skip head-row

                    // check default constructor
                    Constructor<?> defaultConstructor = null;
                    for (Constructor<?> constructor : sheetClass.getDeclaredConstructors()) {
                        if (constructor.getParameterCount() == 0) {
                            defaultConstructor = constructor;
                            break;
                        }
                    }
                    if (defaultConstructor == null) {
                        throw new RuntimeException("ExcelTool readSheet error, sheetClass["+ sheetClass.getName() +"] does not have default constructor.");
                    }

                    // build row object
                    Object rowObj = sheetClass.newInstance();
                    // write row data
                    for (int i = 0; i < headRow.getLastCellNum(); i++) {    // process cell / field

                        // load cell
                        Cell cell = rowX.getCell(i);
                        if (cell == null) {
                            continue;
                        }

                        // match field
                        Field field = null;
                        if (cellIndex2fieldName.containsKey(i)) {
                            field = fieldMame2FieldMap.get(cellIndex2fieldName.get(i));
                        }
                        if (field == null) {
                            continue;
                        }

                        // read cell-value string
                        String cellValueStr = FORMATTER.formatCellValue(cell);

                        // convert 2 field-object
                        Object cellValue = FieldReflectionUtil.parseValue(field, cellValueStr);
                        if (cellValue == null) {
                            continue;
                        }

                        // write field-data
                        field.setAccessible(true);
                        field.set(rowObj, cellValue);
                    }
                    sheetData.add(rowObj);
                }
                rowIndex++;
            }
            return sheetData;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("ExcelTool readSheet error, " + e.getMessage(), e);
        }
    }

    // ---------------------- write Excel ----------------------

    /**
     * 生成Excel，写入磁盘文件
     *
     * @param filePath          file path
     * @param sheetDataList     excel sheet data
     */
    public static void writeFile(String filePath, List<?>... sheetDataList){
        // valid
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new RuntimeException("ExcelTool writeFile error, filePath is empty.");
        }

        // excel type valid
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xls")) {
            throw new RuntimeException("ExcelTool not support Excel 2003 (.xls): " + filePath);
        }

        // write
        try (Workbook workbook = createWorkbook(sheetDataList);
             FileOutputStream fileOutputStream = new FileOutputStream(filePath) ) {

            // write workbook 2 file
            workbook.write(fileOutputStream);       // cover data
            fileOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("ExcelTool writeFile error, filePath: " + filePath, e);
        }
    }

    /**
     * 生成Excel，写入字节数组
     *
     * @param sheetDataList     excel sheet data
     */
    public static byte[] writeByteArray(List<?>... sheetDataList){
        // valid
        if (sheetDataList == null || sheetDataList.length == 0) {
            throw new RuntimeException("ExcelTool writeByteArray error, sheetDataList is empty.");
        }

        // write
        try (Workbook workbook = createWorkbook(sheetDataList);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            // write workbook 2 byteArray
            workbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("ExcelTool writeByteArray error.", e);
        }
    }


    // ---------------------- read Excel ----------------------

    /**
     * 从输入流读取Excel，封装成Java对象
     *
     * @param inputStream   input stream
     * @param sheetClass    sheet class
     * @return List<Object>
     */
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> sheetClass) {
        try (Workbook workbook = WorkbookFactory.create(inputStream);) {
            return (List<T>) readSheet(workbook, sheetClass);
        } catch (IOException | EncryptedDocumentException e) {
            throw new RuntimeException("ExcelTool readExcel error, " + e.getMessage(), e);
        }
    }

    /**
     * 从文件读取Excel，封装成Java对象
     *
     * @param excelFile     excel file
     * @param sheetClass    sheet class
     * @return List<Object>
     */
    public static <T> List<T> readExcel(File excelFile, Class<T> sheetClass) {
        // valid
        if (excelFile == null || !excelFile.exists()) {
            throw new RuntimeException("ExcelTool readExcel error, excelFile is null or not exists.");
        }

        // excel type valid
        String lowerPath = excelFile.getPath().toLowerCase();
        if (lowerPath.endsWith(".xls")) {
            throw new RuntimeException("ExcelTool not support Excel 2003 (.xls): " + lowerPath);
        }

        try {
            return readExcel(Files.newInputStream(excelFile.toPath()), sheetClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件读取Excel，封装成Java对象
     *
     * @param filePath      excel file path
     * @param sheetClass    sheet class
     * @return List<Object>
     */
    public static <T> List<T> readExcel(String filePath, Class<T> sheetClass) {
        return readExcel(new File(filePath), sheetClass);
    }

}
