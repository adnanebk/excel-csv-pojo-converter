package com.adnanebk.excelcsvconverter.excelcsv.core.heplers;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.core.rows_handlers.ExcelRowsHandler;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ExcelHelper<T> {

    private final ExcelRowsHandler<T> rowsHandler;
    private final String[] headers;

    private ExcelHelper(ExcelRowsHandler<T> rowsHandler, String[] headers) {
        this.rowsHandler = rowsHandler;
        this.headers= headers;
    }

    public static <T> ExcelHelper<T> create(Class<T> type) {
        return create(type,null);
    }
    public static <T> ExcelHelper<T> create(Class<T> type,String[] headers) {
        var reflectionHelper = new ReflectionHelper<>(type);
        var rowsHandler = new ExcelRowsHandler<>(reflectionHelper);
        return new ExcelHelper<>(rowsHandler,Optional.ofNullable(headers)
                 .orElse(reflectionHelper.getHeaders().toArray(String[]::new)));
    }


    public Stream<T> toStream(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .filter(this::hasAnyCell)
                    .skip(1)
                    .map(rowsHandler::convertToObject);
        }
    }

    public ByteArrayInputStream toExcel(List<T> list) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            createHeaders(sheet, workbook);
            for (int i = 0; i < list.size(); i++) {
                this.rowsHandler.fillRowFromObject(sheet.createRow(i+1), list.get(i));
            }
            autoSizeColumns(sheet);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new ExcelValidationException("fail to import data to Excel file: ");
        }
    }

    private void createHeaders(Sheet sheet, Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        headerStyle.setFont(font);
        Row headerRow = sheet.createRow(0);
        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            Cell cell = headerRow.createCell(colIdx);
            cell.setCellValue(headers[colIdx]);
            cell.setCellStyle(headerStyle);
        }
    }
    private boolean hasAnyCell(Row currentRow) {
        return currentRow.getPhysicalNumberOfCells() > 0;
    }
    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < sheet.getLastRowNum(); i++)
            sheet.autoSizeColumn(i);
    }
}