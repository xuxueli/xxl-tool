package com.xxl.tool.excel;

import com.xxl.tool.core.ArrayTool;
import com.xxl.tool.core.AssertTool;
import com.xxl.tool.core.CollectionTool;
import com.xxl.tool.core.StringTool;
import com.xxl.tool.excel.annotation.ExcelField;
import com.xxl.tool.excel.annotation.ExcelSheet;
import com.xxl.tool.excel.util.ExcelFieldUtil;
import com.xxl.tool.io.FileTool;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Excel 导入/导出工具：基于注解在 Java 对象集合与 Excel（.xlsx）之间转换。
 *
 * <p>通过 {@link ExcelSheet} 指定 Sheet 名称/表头颜色，通过
 * {@link ExcelField} 指定列名、列宽、对齐、日期格式及是否忽略；未标注注解的
 * 非静态字段按字段名参与映射。列顺序取 {@code getDeclaredFields()} 的声明顺序，
 * 仅处理当前类字段（不含父类字段）。写入时每个对象集合/Supplier 对应一个 Sheet，
 * 第 0 行为表头，数据从第 1 行开始；读取时以第 0 行为表头，按列名匹配字段，
 * 跳过表头行并返回其余行。
 *
 * <p>写入时所有字段值统一转为字符串写入单元格，数字与日期不设置 Excel 数字
 * 格式（日期按 {@link ExcelField#dateformat()} 格式化）；读取时使用
 * {@link DataFormatter} 将单元格按显示格式转为文本，再按字段类型解析。
 *
 * <p>文件与流的生命周期：
 * <ul>
 *     <li>写入文件的重载要求目标文件不存在（存在即抛出异常），并自动创建父目录；
 *     Workbook 与文件流由方法内部 try-with-resources 关闭；</li>
 *     <li>读取时 Workbook 由方法内部关闭；由 File/路径重载打开的输入流随
 *     Workbook 释放，直接传入的 {@link InputStream} 建议由调用方自行关闭；</li>
 *     <li>读取会把整个 Workbook 载入内存（POI 非 SAX 流式），Consumer 重载只是
 *     逐行回调，并不降低内存占用；写入 Supplier 重载按批取数，避免一次性构造
 *     全部数据。</li>
 * </ul>
 *
 * <p>仅支持 Excel 2007+（.xlsx），路径以小写后 {@code .xls} 结尾时直接拒绝；
 * 依赖 POI（provided，需使用方自行引入）。工具类不保存业务状态，Workbook 仅在
 * 方法内部创建和关闭、不对外暴露；读取共用静态 {@link DataFormatter} 且未额外
 * 同步，严格并发场景请由调用方串行化。
 *
 * @author xuxueli 2017-09-08 22:27:20
 */
public class ExcelTool {
    private static final Logger logger = LoggerFactory.getLogger(ExcelTool.class);


    // ---------------------- write Workbook ----------------------

    /**
     * create Workbook with SheetData
     */
    private static Workbook createWorkbook(List<?>... sheetDataList){

        // valid
        if (ArrayTool.isEmpty(sheetDataList)) {
            throw new RuntimeException("ExcelTool createWorkbook error, sheetData can not be empty.");
        }

        // build Workbook
        Workbook workbook = new XSSFWorkbook();

        // write sheet-data
        for (List<?> sheetData: sheetDataList) {
            // parse sheet-class
            Class<?> sheetClass = (Class<?>) sheetData.get(0).getClass();

            // create sheet
            createSheet(workbook, sheetClass, sheetData, null);
        }

        return workbook;
    }

    /**
     * create Workbook with Supplier
     */
    private static Workbook createWorkbook(Supplier<?>... suppliers){

        // valid
        if (ArrayTool.isEmpty(suppliers)) {
            throw new RuntimeException("ExcelTool createWorkbook error, consumers can not be empty.");
        }

        // build Workbook
        Workbook workbook = new XSSFWorkbook();

        // write sheet-data
        for (Supplier<?> supplier: suppliers) {
            // parse sheet-class
            ParameterizedType parameterizedType = (ParameterizedType) supplier.getClass().getGenericInterfaces()[0];
            Type actualType = parameterizedType.getActualTypeArguments()[0];

            // parse data-class
            Class<?> sheetClass = null;
            if (actualType instanceof ParameterizedType listType) {
                // 1、list
                if (listType.getRawType().equals(List.class)) {
                    sheetClass =  (Class<?>) listType.getActualTypeArguments()[0];
                }
            } else {
                // 2、default
                sheetClass = (Class<?>) actualType;
            }

            // create sheet
            createSheet(workbook, sheetClass, null, supplier);
        }

        return workbook;
    }

    /**
     * create Sheet
     */
    private static void createSheet(Workbook workbook,
                                    Class<?> sheetClass,
                                    List<?> sheetData,
                                    Supplier<?> supplier){

        AssertTool.notNull(workbook, "workbook can not be null.");
        AssertTool.notNull(sheetClass, "sheetClass can not be null.");

        // 1、parse sheet-class
        ExcelSheet excelSheetAnno = sheetClass.getAnnotation(ExcelSheet.class);
        String sheetName = sheetClass.getSimpleName();
        short headColorIndex = -1;
        if (excelSheetAnno != null) {
            if (excelSheetAnno.name()!=null && !excelSheetAnno.name().trim().isEmpty()) {
                sheetName = excelSheetAnno.name().trim();
            }
            headColorIndex = excelSheetAnno.headColor().getIndex();
        }

        // 2、parse sheet-field
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

        // 3、create Sheet
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

        // 4、create header-row
        int[] fieldWidthArr = new int[fields.size()];
        CellStyle[] fieldDataStyleArr = new CellStyle[fields.size()];
        Row headRow = sheet.createRow(0);

        // 5、write each rows
        for (int i = 0; i < fields.size(); i++) {

            // 5.1、parse field config
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

            // 5.2、collect field width + aligh
            fieldWidthArr[i] = fieldWidth;
            CellStyle fieldDataStyle = workbook.createCellStyle();
            if (align != null) {
                fieldDataStyle.setAlignment(align);
            }
            fieldDataStyleArr[i] = fieldDataStyle;

            // 5.3、create head
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

        // 6、write sheet-data rows
        int rowIndex = 0;
        if (CollectionTool.isNotEmpty(sheetData)) {
            for (Object rowData: sheetData){
                writeRowData(sheet, fields, fieldDataStyleArr, rowIndex++, rowData);
            }
        }
        if (supplier != null) {
            Object rowData = supplier.get();
            while (rowData != null) {
                // process
                if (rowData instanceof List<?> rowList) {
                    // 1、list
                    for (Object rowListItem : rowList) {
                        writeRowData(sheet, fields, fieldDataStyleArr, rowIndex++, rowListItem);
                    }
                } else {
                    // 2、default
                    writeRowData(sheet, fields, fieldDataStyleArr, rowIndex++, rowData);
                }

                // next data
                rowData = supplier.get();
            }
        }

        // 7、write sheet-width
        writeColumnWidth(sheet, fields, fieldWidthArr);
    }

    /**
     * write List<Object> 2 sheet-data
     */
    private static void writeRowData(Sheet sheet,
                                     List<Field> fields,
                                     CellStyle[] fieldDataStyleArr,
                                     int index,
                                     Object rowData){

        // prepare row + row-data
        Row rowX = sheet.createRow(index + 1);      // skip head-row

        // write row-data
        for (int i = 0; i < fields.size(); i++) {
            try {
                // parse field-value
                Field field = fields.get(i);
                field.setAccessible(true);
                Object fieldValue = field.get(rowData);

                // convert to string
                String fieldValueString = ExcelFieldUtil.formatValue(field, fieldValue);

                // write row-data
                Cell cellX = rowX.createCell(i, CellType.STRING);       //rowX.createCell(i);
                cellX.setCellStyle(fieldDataStyleArr[i]);
                cellX.setCellValue(fieldValueString);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("ExcelTool createSheet error, write row-data error.", e);
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


    // ---------------------- write Excel (default) ----------------------

    /**
     * 将一个或多个对象集合写入 Excel 文件（.xlsx）。
     *
     * <p>每个 {@code List} 生成一个 Sheet，Sheet 名称/样式取集合首元素类上的
     * 注解，因此集合不能为空；第 0 行为表头，其后每行一个对象。目标文件必须
     * 不存在，父目录会自动创建；路径以 {@code .xls} 结尾（忽略大小写）时拒绝。
     * Workbook 与文件流由方法内部关闭。
     *
     * <pre>
     *     ExcelTool.writeExcel("/tmp/users.xlsx", userList, shopList);
     * </pre>
     *
     * @param filePath 目标文件路径，不能为空白
     * @param sheetDataList 每个元素对应一个 Sheet 的对象集合，不能为空
     * @throws RuntimeException 路径为空、目标已存在、集合为空、类型/字段非法或写入失败
     */
    public static void writeExcel(String filePath, List<?>... sheetDataList){
        // valid
        if (StringTool.isBlank(filePath)) {
            throw new RuntimeException("ExcelTool writeFile error, filePath is empty.");
        }

        // excel file type
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xls")) {
            throw new RuntimeException("ExcelTool not support Excel 2003 (.xls): " + filePath);
        }

        // check file exists
        if (FileTool.exists(filePath)) {
            throw new RuntimeException("ExcelTool writeFile error, filePath: " + filePath + " already exists.");
        } else {
            try {
                FileTool.createParentDirectories(FileTool.file(filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
     * 将一个或多个对象集合写入 Excel 并返回文件字节（.xlsx）。
     *
     * <p>每个 {@code List} 生成一个 Sheet，映射规则与写文件重载一致；结果保存
     * 在内存中，适合直接作为下载响应，数据量很大时请改用写文件重载。Workbook
     * 与输出流由方法内部关闭。
     *
     * <pre>
     *     byte[] bytes = ExcelTool.writeExcel(userList, shopList);
     * </pre>
     *
     * @param sheetDataList 每个元素对应一个 Sheet 的对象集合，不能为空
     * @return xlsx 文件字节数组
     * @throws RuntimeException 集合为空、类型/字段非法或写入失败
     */
    public static byte[] writeExcel(List<?>... sheetDataList){
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


    // ---------------------- write Excel (stream) ----------------------

    /**
     * 使用 {@link Supplier} 分批取数，写入单个 Sheet 的 Excel 文件（.xlsx）。
     *
     * <p>Sheet 对应的类由 Supplier 的泛型参数（匿名实现类实现的泛型接口）推断；
     * 每次 {@code get()} 可返回单个对象，也可返回 {@code List}（批量），返回
     * {@code null} 时结束。数据不会一次性全部载入内存。目标文件必须不存在，
     * 父目录自动创建；{@code .xls} 后缀拒绝。Workbook 与文件流由方法内部关闭。
     *
     * <pre>
     *     ExcelTool.writeExcel("/tmp/users.xlsx", new Supplier&lt;UserDTO&gt;() {
     *         public UserDTO get() { return next(); }   // 无数据时返回 null
     *     });
     * </pre>
     *
     * @param filePath 目标文件路径，不能为空白
     * @param suppliers 数据提供者，生成一个 Sheet；不能为 {@code null}
     * @throws RuntimeException 路径为空、目标已存在、泛型无法解析或写入失败
     */
    public static void writeExcel(String filePath, Supplier<?> suppliers){
        // valid
        if (StringTool.isBlank(filePath)) {
            throw new RuntimeException("ExcelTool writeFile error, filePath is empty.");
        }

        // excel file type
        String lowerPath = filePath.toLowerCase();
        if (lowerPath.endsWith(".xls")) {
            throw new RuntimeException("ExcelTool not support Excel 2003 (.xls): " + filePath);
        }

        // check file exists
        if (FileTool.exists(filePath)) {
            throw new RuntimeException("ExcelTool writeFile error, filePath: " + filePath + " already exists.");
        } else {
            try {
                FileTool.createParentDirectories(FileTool.file(filePath));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // write
        try (Workbook workbook = createWorkbook(suppliers);
             FileOutputStream fileOutputStream = new FileOutputStream(filePath) ) {

            // write workbook 2 file
            workbook.write(fileOutputStream);       // cover data
            fileOutputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException("ExcelTool writeFile error, filePath: " + filePath, e);
        }
    }

    /**
     * 使用 {@link Supplier} 分批取数，写入 Excel 并返回文件字节（.xlsx）。
     *
     * <p>每个 Supplier 生成一个 Sheet；每次 {@code get()} 可返回单个对象或
     * {@code List}（批量），返回 {@code null} 时结束，数据按批处理，避免一次性
     * 构造全部对象。Workbook 与输出流由方法内部关闭。
     *
     * <pre>
     *     byte[] bytes = ExcelTool.writeExcel(userSupplier);
     * </pre>
     *
     * @param suppliers 数据提供者，每个生成一个 Sheet，不能为空
     * @return xlsx 文件字节数组
     * @throws RuntimeException 数据提供者为空、泛型无法解析或写入失败
     */
    public static byte[] writeExcel(Supplier<?>... suppliers){
        // valid
        if (ArrayTool.isEmpty(suppliers)) {
            throw new RuntimeException("ExcelTool writeByteArray error, sheetDataList is empty.");
        }

        // write
        try (Workbook workbook = createWorkbook(suppliers);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            // write workbook 2 byteArray
            workbook.write(byteArrayOutputStream);
            byteArrayOutputStream.flush();
            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("ExcelTool writeByteArray error.", e);
        }
    }


    // ---------------------- read Workbook ----------------------

    /**
     * 单元格显示值格式化器：读取时按单元格自身格式把值转为文本
     */
    private static final DataFormatter FORMATTER = new DataFormatter();

    /**
     * 读取 Sheet 数据：可写入 {@code sheetData} 或逐行回调 {@code consumer}。
     *
     * <p>Sheet 名称取 {@code @ExcelSheet.name()} 或类简单名；第 0 行为表头，
     * 其后每行实例化一个对象并按表头列名填充字段。Sheet 或表头不存在时直接返回。
     *
     * @param workbook 工作簿，不能为 {@code null}
     * @param sheetClass 行对象类型，必须有无参且可访问的构造器
     * @param sheetData 结果收集列表，可为 {@code null}（只回调）
     * @param consumer 行回调，可为 {@code null}（只收集）
     * @param <T> 行对象类型
     */
    private static <T> void readSheet(Workbook workbook,
                                      Class<T> sheetClass,
                                      List<T> sheetData,
                                      Consumer<T> consumer) {
        AssertTool.notNull(workbook, "workbook can not be null.");
        AssertTool.notNull(sheetClass, "sheetClass can not be null.");

        try {
            // 1、parse sheet
            ExcelSheet excelSheet = sheetClass.getAnnotation(ExcelSheet.class);
            String sheetName = (excelSheet!=null && excelSheet.name()!=null && !excelSheet.name().trim().isEmpty())
                    ?excelSheet.name().trim()
                    :sheetClass.getSimpleName();

            /**
             * 2、parse sheet-Field
             *
             *  - fieldMame2FieldMap: fieldName -> Field
             *  - cellIndex2fieldName: index -> fieldName
             */
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

            // parse fieldName 2 index, from head-row
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                return;
            }
            Row headRow = sheet.getRow(0);
            if (headRow == null) {
                return;
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

            // 3、process each row
            Iterator<Row> sheetIterator = sheet.rowIterator();
            int rowIndex = 0;
            while (sheetIterator.hasNext()) {
                Row rowX = sheetIterator.next();
                if (rowIndex > 0) {     // skip head-row

                    // 3.1、valid default constructor
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

                    // 3.2、build row-object
                    T rowObj = sheetClass.cast(defaultConstructor.newInstance());

                    // 3.3、fill each field
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
                        Object cellValue = ExcelFieldUtil.parseValue(field, cellValueStr);
                        if (cellValue == null) {
                            continue;
                        }

                        // write field-data
                        field.setAccessible(true);
                        field.set(rowObj, cellValue);
                    }

                    // 3.4、collect row-data
                    if (sheetData != null) {
                        sheetData.add(rowObj);
                    }
                    if (consumer != null) {
                        consumer.accept(rowObj);
                    }

                }
                rowIndex++;
            }
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("ExcelTool readSheet error, " + e.getMessage(), e);
        }
    }


    // ---------------------- read Excel (default)  ----------------------

    /**
     * 从输入流读取 Excel（.xlsx）并转换为对象列表。
     *
     * <p>按 {@code sheetClass} 上的 {@link ExcelSheet#name()}（缺省为类简单名）
     * 查找 Sheet，以第 0 行为表头、按列名匹配字段（字段名或
     * {@link ExcelField#name()}），跳过表头后逐行转换；未知列、空单元格与空白值
     * 跳过。Sheet 或表头行不存在时返回空列表。类必须有无参且可访问的构造器。
     *
     * <p>Workbook 由方法内部关闭，整个工作簿会载入内存（非 SAX 流式）；直接传入
     * 的输入流建议由调用方自行关闭。
     *
     * <pre>
     *     List&lt;UserDTO&gt; list = ExcelTool.readExcel(inputStream, UserDTO.class);
     * </pre>
     *
     * @param inputStream Excel 输入流，不能为 {@code null}
     * @param sheetClass 目标类型，不能为 {@code null}
     * @param <T> 目标类型
     * @return 数据行对象列表（不含表头），无匹配数据时返回空列表
     * @throws RuntimeException 读取失败、文件加密、字段类型不支持或实例化失败
     */
    public static <T> List<T> readExcel(InputStream inputStream, Class<T> sheetClass) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            // build sheetData
            List<T> sheetData = new ArrayList<>();

            // read sheet
            readSheet(workbook, sheetClass, sheetData, null);

            // return
            return sheetData;
        } catch (IOException | EncryptedDocumentException e) {
            throw new RuntimeException("ExcelTool readExcel error, " + e.getMessage(), e);
        }
    }

    /**
     * 从 Excel 文件读取并转换为对象列表。
     *
     * <p>文件必须存在且不以 {@code .xls} 结尾（忽略大小写）；内部打开文件流并
     * 交给 {@link #readExcel(InputStream, Class)} 处理，Workbook 关闭时释放。
     * 映射规则（Sheet 名称、表头、字段匹配、空值）见该方法。
     *
     * <pre>
     *     List&lt;UserDTO&gt; list = ExcelTool.readExcel(excelFile, UserDTO.class);
     * </pre>
     *
     * @param excelFile Excel 文件，不能为 {@code null} 且必须存在
     * @param sheetClass 目标类型，不能为 {@code null}
     * @param <T> 目标类型
     * @return 数据行对象列表（不含表头）
     * @throws RuntimeException 文件为空/不存在、为 .xls、读取或转换失败
     */
    public static <T> List<T> readExcel(File excelFile, Class<T> sheetClass) {
        // valid
        if (excelFile == null || !excelFile.exists()) {
            throw new RuntimeException("ExcelTool readExcel error, excelFile is null or not exists.");
        }

        // valid file type
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
     * 从 Excel 文件路径读取并转换为对象列表。
     *
     * <p>等价于 {@code readExcel(new File(filePath), sheetClass)}；映射规则见
     * {@link #readExcel(InputStream, Class)}。
     *
     * <pre>
     *     ExcelTool.readExcel("/xxx/excelFile.xlsx", UserDTO.class);
     * </pre>
     *
     * @param filePath Excel 文件路径
     * @param sheetClass 目标类型，不能为 {@code null}
     * @param <T> 目标类型
     * @return 数据行对象列表（不含表头）
     * @throws RuntimeException 文件不存在、为 .xls、读取或转换失败
     */
    public static <T> List<T> readExcel(String filePath, Class<T> sheetClass) {
        return readExcel(new File(filePath), sheetClass);
    }

    // ---------------------- read Excel (stream)  ----------------------

    /**
     * 从输入流读取 Excel，并逐行回调 {@link Consumer}（不收集结果）。
     *
     * <p>目标类型通过匿名 Consumer 的泛型接口推断（如
     * {@code new Consumer<UserDTO>() {...}}），因此必须使用带具体类型参数的
     * 匿名实现类；映射规则同 {@link #readExcel(InputStream, Class)}。整个
     * Workbook 会载入内存，回调只是逐行处理，并非 SAX 流式读取；Workbook 由
     * 方法内部关闭。
     *
     * <pre>
     *     ExcelTool.readExcel(inputStream, new Consumer&lt;UserDTO&gt;() {
     *         public void accept(UserDTO user) { ... }
     *     });
     * </pre>
     *
     * @param inputStream Excel 输入流，不能为 {@code null}
     * @param consumer 行消费者，不能为 {@code null}，且需为带具体类型参数的匿名实现
     * @param <T> 行类型
     * @throws RuntimeException 读取/转换失败或无法从 Consumer 解析目标类型
     */
    public static <T> void readExcel(InputStream inputStream, Consumer<T> consumer) {
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {

            // get sheetClass
            ParameterizedType parameterizedType = (ParameterizedType) consumer.getClass().getGenericInterfaces()[0];
            Type sheetType = parameterizedType.getActualTypeArguments()[0];
            Class<T> sheetClass = (Class<T>) sheetType;

            // read sheet
            readSheet(workbook, sheetClass, null, consumer);
        } catch (IOException | EncryptedDocumentException e) {
            throw new RuntimeException("ExcelTool readExcel error, " + e.getMessage(), e);
        }
    }

    /**
     * 从 Excel 文件读取并逐行回调 {@link Consumer}。
     *
     * <p>文件必须存在且不以 {@code .xls} 结尾（忽略大小写）；目标类型由匿名
     * Consumer 的泛型参数推断。其余规则见
     * {@link #readExcel(InputStream, Consumer)}。
     *
     * <pre>
     *     ExcelTool.readExcel(excelFile, new Consumer&lt;UserDTO&gt;() {
     *         public void accept(UserDTO user) { ... }
     *     });
     * </pre>
     *
     * @param excelFile Excel 文件，不能为 {@code null} 且必须存在
     * @param consumer 行消费者，不能为 {@code null}
     * @param <T> 行类型
     * @throws RuntimeException 文件为空/不存在、为 .xls、读取/转换失败或类型无法解析
     */
    public static <T> void readExcel(File excelFile, Consumer<T> consumer) {
        // valid
        if (excelFile == null || !excelFile.exists()) {
            throw new RuntimeException("ExcelTool readExcel error, excelFile is null or not exists.");
        }

        // valid file type
        String lowerPath = excelFile.getPath().toLowerCase();
        if (lowerPath.endsWith(".xls")) {
            throw new RuntimeException("ExcelTool not support Excel 2003 (.xls): " + lowerPath);
        }

        try {
            readExcel(Files.newInputStream(excelFile.toPath()), consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从 Excel 文件路径读取并逐行回调 {@link Consumer}。
     *
     * <p>等价于 {@code readExcel(new File(filePath), consumer)}；目标类型由匿名
     * Consumer 的泛型参数推断，其余规则见
     * {@link #readExcel(InputStream, Consumer)}。
     *
     * <pre>
     *     ExcelTool.readExcel("/xxx/excelFile.xlsx", new Consumer&lt;UserDTO&gt;() {
     *         public void accept(UserDTO user) { ... }
     *     });
     * </pre>
     *
     * @param filePath Excel 文件路径
     * @param consumer 行消费者，不能为 {@code null}
     * @param <T> 行类型
     * @throws RuntimeException 文件不存在、为 .xls、读取/转换失败或类型无法解析
     */
    public static <T> void readExcel(String filePath, Consumer<T> consumer) {
        readExcel(new File(filePath), consumer);
    }

}
