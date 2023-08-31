package com.example.excelConverter.excel;


import com.example.excelConverter.excel.exceptions.ExcelFileException;
import com.example.excelConverter.excel.exceptions.ExcelValidationException;
import com.example.excelConverter.excel.models.AnnotationType;
import com.example.excelConverter.excel.models.Field;

import com.example.excelConverter.excel.utils.ReflectionUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;


public class ExcelHelper<T>   {

    private static final  String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final   String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private  final String sheetName;
    private final ReflectionUtil<T> reflectionUtil;

    private ExcelHelper(Class<T> type, AnnotationType annotationType) {
        reflectionUtil = new ReflectionUtil<>(type,annotationType);
        sheetName = type.getSimpleName()+"-"+ LocalDate.now();

    }
    public static<T> ExcelHelper<T> create(Class<T> type, AnnotationType annotationType) {
        return new ExcelHelper<>(type,annotationType);
    }
    public static<T> ExcelHelper<T> create(Class<T> type) {
        return create(type,AnnotationType.FIELD);
    }
    public List<T> excelToList(MultipartFile file) {
        if (!hasExcelFormat(file))
            throw new ExcelFileException("Only excel formats are valid");
        try(InputStream is = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(is)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            return StreamSupport.stream(sheet.spliterator(),false)
                     .skip(1)
                     .filter(this::hasAnyCell)
                     .map(this::rowToObject)
                     .toList();

        }
        catch (IOException e) {
            throw new ExcelFileException("fail to parse Excel file: " + e.getMessage());
        }
    }

    private T rowToObject(Row currentRow) {
        List<Field> fields = reflectionUtil.getFields();
        Object[] values = new Object[fields.size()];
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            values[i] = getCurrentCell(i++, currentRow).map(cell -> getCellValue(cell, field))
                    .orElse(null);
        }
        return reflectionUtil.getInstance(values);

    }

    private Object  getCellValue(Cell cell, Field field) {
        try {
            if (reflectionUtil.isNumberType(field))
                return getValueAsNumber(cell,field);
            if (reflectionUtil.isStringValue(field))
                return cell.getStringCellValue().trim();
            if (reflectionUtil.isBooleanValue(field))
                return cell.getBooleanCellValue();
            if (reflectionUtil.isEnumValue(field))
                return Enum.valueOf(field.type().asSubclass(Enum.class),cell.getStringCellValue().toUpperCase().trim());
            if(cell.getCellType().equals(CellType.NUMERIC)) {
                if(reflectionUtil.isAnyDateTimeValue(field))
                    cell.setCellValue(reflectionUtil.localedDateTimeFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
                else cell.setCellValue(reflectionUtil.localedDateFormatter.format(DateUtil.getLocalDateTime(cell.getNumericCellValue())));
            }
            if (reflectionUtil.isDateValue(field))
                return reflectionUtil.dateFormatter.parse(cell.getStringCellValue());
            if (reflectionUtil.isLocalDateValue(field))
                return LocalDate.parse(cell.getStringCellValue(),reflectionUtil.localedDateFormatter);
            if (reflectionUtil.isLocalDateTimeValue(field))
                return LocalDateTime.parse(cell.getStringCellValue(),reflectionUtil.localedDateTimeFormatter);
            if (reflectionUtil.isZonedDateValue(field))
                return ZonedDateTime.parse(cell.getStringCellValue(),reflectionUtil.zonedDateTimeFormatter);
        }
        catch (ParseException | IllegalStateException e ) {
            throw new ExcelValidationException(String.format("Invalid format in row %s, column %s",cell.getRowIndex()+1,ALPHABET.charAt(cell.getColumnIndex())));
        }
        throw new ExcelFileException("Can't find a corresponding type of the cell");
    }




    private Number getValueAsNumber(Cell cell, Field field) {
        if (reflectionUtil.isIntegerValue(field))
            return (int) cell.getNumericCellValue();
        else if (reflectionUtil.isLongValue(field))
            return (long) cell.getNumericCellValue();
        else if (reflectionUtil.isDoubleValue(field))
            return  cell.getNumericCellValue();
        return cell.getNumericCellValue();
    }

    public ByteArrayInputStream listToExcel(List<T> list) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = createSheet(sheetName,workbook, reflectionUtil.getFields().stream().map(Field::title).toArray(String[]::new));
            for(int i=1;i<list.size();i++){
                fillRowFromObject(sheet.createRow(i), list.get(i));
            }
            autoSizeColumns(sheet);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new ExcelValidationException("fail to import data to Excel file: ");
        }
    }

    private void autoSizeColumns(Sheet sheet) {
       IntStream.range(0,reflectionUtil.getFields().size()).forEach(sheet::autoSizeColumn);
    }

    private void fillRowFromObject(Row row, T obj) {
        List<Field> fields = reflectionUtil.getFields();
        for (int i = 0; i < fields.size(); i++) {
            Field field = fields.get(i);
            Object fieldValue = reflectionUtil.getFieldValue(obj, i);
            if (reflectionUtil.isNumberType(field))
                row.createCell(i).setCellValue(Double.parseDouble(fieldValue.toString()));
            else if (reflectionUtil.isStringValue(field))
                row.createCell(i).setCellValue(fieldValue.toString());
            else if (reflectionUtil.isBooleanValue(field))
                row.createCell(i).setCellValue((boolean) fieldValue);
            else if (reflectionUtil.isEnumValue(field))
                row.createCell(i).setCellValue(fieldValue.toString());
            else if (reflectionUtil.isDateValue(field))
                row.createCell(i).setCellValue(reflectionUtil.dateFormatter.format(fieldValue));
            else if (reflectionUtil.isLocalDateValue(field))
                row.createCell(i).setCellValue(reflectionUtil.localedDateFormatter.format((LocalDate) fieldValue));
            else if (reflectionUtil.isLocalDateTimeValue(field))
                row.createCell(i).setCellValue(reflectionUtil.localedDateTimeFormatter.format((LocalDateTime) fieldValue));
            else if (reflectionUtil.isZonedDateValue(field))
                row.createCell(i).setCellValue(reflectionUtil.zonedDateTimeFormatter.format((ZonedDateTime) fieldValue));
        }

    }




    private boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    private  Sheet createSheet(String sheetName,Workbook workbook,String[] headers) {
        Sheet sheet = workbook.createSheet(sheetName);
        Row headerRow = sheet.createRow(0);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);
        headerStyle.setFont(font);

        for (int colIdx = 0; colIdx < headers.length; colIdx++) {
            Cell cell = headerRow.createCell(colIdx);
            cell.setCellValue(headers[colIdx]);
            cell.setCellStyle(headerStyle);
        }
        return sheet;
    }
    private boolean hasAnyCell(Row currentRow) {
        return currentRow.getPhysicalNumberOfCells() > 0;
    }


    private Optional<Cell> getCurrentCell(int colIndex, Row currentRow) {
        return Optional.ofNullable(currentRow.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }


}