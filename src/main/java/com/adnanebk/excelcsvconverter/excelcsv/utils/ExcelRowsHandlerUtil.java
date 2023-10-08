package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.models.SheetField;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ExcelRowsHandlerUtil<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final DateParserFormatterUtil dateParserFormatterUtil;
    private final ReflectionUtil<T> reflectionUtil;


    public ExcelRowsHandlerUtil(ReflectionUtil<T> reflectionUtil,DateParserFormatterUtil dateParserFormatterUtil) {
        this.reflectionUtil = reflectionUtil;
        this.dateParserFormatterUtil = dateParserFormatterUtil;
    }

    public void fillRowFromObject(Row row, T obj) {
        var fields = reflectionUtil.getFields();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            setCellValue(field.typeName(), row.createCell(i),field.getValue(obj));
        }
    }

    public T createObjectFromRow(Row currentRow) {
        var fields = reflectionUtil.getFields();
        Object[] values = fields.stream()
                .map(field -> getCurrentCell(field.cellIndex(), currentRow)
                        .map(cell -> getCellValue(field.typeName(),cell))
                        .orElse(null)
                ).toArray();
        return createObjectAndSetFieldsValues(values,fields);
    }
    public String[] getHeaders() {
        return reflectionUtil.getFields().stream().map(SheetField::title).toArray(String[]::new);
    }

    private T createObjectAndSetFieldsValues(Object[] values, List<SheetField<T>> fields) {
        T obj = reflectionUtil.createInstance();
        for (int i = 0; i < values.length; i++) {
            fields.get(i).setValue(obj, values[i]);
        }
        return obj;
    }

    private Object getCellValue(String typeName, Cell cell) {
        try {
            return switch (typeName) {
                case "string","enum" -> cell.getStringCellValue();
                case "boolean" -> cell.getCellType()==CellType.BOOLEAN?cell.getBooleanCellValue():cell.getStringCellValue();
                case "integer", "int" -> (int) cell.getNumericCellValue();
                case "short" -> (short) cell.getNumericCellValue();
                case "long" -> (long) cell.getNumericCellValue();
                case "double" -> cell.getNumericCellValue();
                case "localdate" -> getAsLocalDate(cell);
                case "localdatetime" -> getAsLocalDateTime(cell);
                case "zoneddatetime" -> getAsZonedDateTime(cell);
                case "date" -> getAsDate(cell);
                default -> throw new ExcelValidationException("Unsupported field typeName "+typeName);
            };
        } catch (IllegalStateException | NumberFormatException e) {
            throw new ExcelValidationException(String.format("Unexpected or Invalid cell value in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex())));
        }
    }

    private void setCellValue(String typeName, Cell cell, Object value) {
        if(value==null)
            return;
        switch (typeName) {
            case "string","enum","boolean" -> cell.setCellValue(value.toString());
            case "double", "float", "integer", "int", "long", "short" -> cell.setCellValue(Double.parseDouble(value.toString()));
            case "localdate" -> cell.setCellValue(dateParserFormatterUtil.format((LocalDate) value));
            case "localdatetime" -> cell.setCellValue(dateParserFormatterUtil.format((LocalDateTime) value));
            case "zoneddatetime" -> cell.setCellValue(dateParserFormatterUtil.format((ZonedDateTime) value));
            case "date" -> cell.setCellValue(dateParserFormatterUtil.format((Date) value));
            default -> throw new ExcelValidationException("Unsupported field typeName");
        }
    }



    private Date getAsDate(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getDateCellValue();
            return dateParserFormatterUtil.parseToDate(cell.getStringCellValue());
        } catch (ParseException e) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private LocalDate getAsLocalDate(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue().toLocalDate();
            return dateParserFormatterUtil.parseToLocalDate(cell.getStringCellValue());
        } catch (DateTimeParseException ex) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private LocalDateTime getAsLocalDateTime(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue();
            return dateParserFormatterUtil.parseToLocalDateTime(cell.getStringCellValue());
        } catch (DateTimeParseException ex) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private ZonedDateTime getAsZonedDateTime(Cell cell) {
        try {
            if (cell.getCellType().equals(CellType.NUMERIC))
                return cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault());
            return dateParserFormatterUtil.parseToZonedDateTime(cell.getStringCellValue());
        } catch (DateTimeParseException ex) {
            throw new ExcelValidationException(getInvalidCellDateMsg(cell));
        }
    }

    private static String getInvalidCellDateMsg(Cell cell) {
        return String.format("Invalid or unsupported date format in row %s, column %s", cell.getRowIndex() + 1, ALPHABET.charAt(cell.getColumnIndex()));
    }

    private Optional<Cell> getCurrentCell(int colIndex, Row currentRow) {
        return Optional.ofNullable(currentRow.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL));
    }

}
