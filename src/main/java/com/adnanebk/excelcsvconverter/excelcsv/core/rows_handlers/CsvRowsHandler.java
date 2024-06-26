package com.adnanebk.excelcsvconverter.excelcsv.core.rows_handlers;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

public class CsvRowsHandler<T> {

    private final DateParserFormatter dateParserFormatter;
    private final ReflectionHelper<T> reflectionHelper;

    public CsvRowsHandler(ReflectionHelper<T> reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
        this.dateParserFormatter = reflectionHelper.getSheetInfo()
                .map(info->new DateParserFormatter(info.datePattern(),info.dateTimePattern()))
                .orElseGet(DateParserFormatter::new);

    }

    public T convertToObject(String row, String delimiter, char quoteChar) {
        String[] cellsValues = row.split(delimiter);
        var fields = reflectionHelper.getFields();
        T obj = reflectionHelper.createInstance();
        for (int i = 0; i < Math.min(cellsValues.length,fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = cellsValues[field.getCellIndex()].replace(quoteChar+"","");
            Object fieldValue = convertToFieldValue(cellValue, field.getTypeName());
            reflectionHelper.getFields().get(i).setValue(fieldValue,obj);
        }
        return obj;
    }

    public String[] convertFieldValuesToStrings(T obj) {
        return  reflectionHelper.getFields().stream()
                .map(field -> {
                    Object value = field.getValue(obj);
                    if(value instanceof Date date)
                        return dateParserFormatter.format(date);
                    if(value instanceof LocalDate date)
                        return dateParserFormatter.format(date);
                    if(value instanceof LocalDateTime date)
                        return dateParserFormatter.format(date);
                    if(value instanceof ZonedDateTime date)
                        return dateParserFormatter.format(date);
                    return value.toString();
                })
                .toArray(String[]::new);
    }

    private Object convertToFieldValue(String cellValue, String fieldType) {
        try {
            return switch (fieldType) {
                case "string","enum","boolean" -> cellValue;
                case "integer", "int" -> Integer.parseInt(cellValue);
                case "short" -> Short.parseShort(cellValue);
                case "long" -> Long.parseLong(cellValue);
                case "double" -> Double.parseDouble(cellValue.replace(",", "."));
                case "localdate" -> dateParserFormatter.parseToLocalDate(cellValue);
                case "localdatetime" -> dateParserFormatter.parseToLocalDateTime(cellValue);
                case "zoneddatetime" -> dateParserFormatter.parseToZonedDateTime(cellValue);
                case "date" -> convertToDate(cellValue);
                default -> throw new ExcelValidationException("Unsupported type: " + fieldType);
            };
        } catch (DateTimeException e) {
            throw new ExcelValidationException(String.format("Invalid or unsupported date pattern for the value %s", cellValue));
        }
        catch (RuntimeException ex){
            throw new ExcelValidationException("Unexpected or Invalid cell value {"+cellValue+"}  ");
        }
    }

    private Date convertToDate(String cellValue) {
            return dateParserFormatter.parseToDate(cellValue);
    }

}
