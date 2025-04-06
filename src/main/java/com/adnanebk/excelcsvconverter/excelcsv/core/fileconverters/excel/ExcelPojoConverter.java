package com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.excel;

import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.FilePojoConverter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public class ExcelPojoConverter<T> implements FilePojoConverter<T> {

    private final ExcelRowsHandler<T> rowsHandler;

    public ExcelPojoConverter(ExcelRowsHandler<T> rowsHandler) {
        this.rowsHandler = rowsHandler;
    }


    @Override
    public Stream<T> toStream(InputStream inputStream) {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(), false)
                    .filter(this::hasAnyCell)
                    .skip(1)
                    .map(rowsHandler::convertToObject);
        } catch (Exception ex) {
            throw new SheetValidationException(ex.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream toByteArrayInputStream(List<T> list) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            createHeaders(sheet, workbook);
            for (int i = 0; i < list.size(); i++) {
                this.rowsHandler.fillRowFromObject(sheet.createRow(i + 1), list.get(i));
            }
            autoSizeColumns(sheet);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new SheetValidationException("fail to import data to Excel file: ");
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
        String[] headers = rowsHandler.getHeaders();
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