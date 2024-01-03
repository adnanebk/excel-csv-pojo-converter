package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ExcelValidationException;
import com.adnanebk.excelcsvconverter.excelcsv.models.SheetField;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

public class CsvRowsHandlerUtil<T> {

    private final DateParserFormatterUtil dateParserFormatterUtil;
    private final ReflectionUtil<T> reflectionUtil;

    public CsvRowsHandlerUtil(ReflectionUtil<T> reflectionUtil,DateParserFormatterUtil dateParserFormatterUtil) {
        this.reflectionUtil = reflectionUtil;
        this.dateParserFormatterUtil=dateParserFormatterUtil;

    }

    public T createObjectFromRow(String row, String delimiter,char quoteChar) {
        String[] cellsValues = row.split(delimiter);
        Object[] values = new Object[cellsValues.length];
        var fields = reflectionUtil.getFields();
        for (int i = 0; i < Math.min(cellsValues.length,fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = cellsValues[field.cellIndex()].replace(quoteChar+"","");
            values[i] = convertToTypedValue(cellValue, field.typeName());
        }
        return createObjectAndSetFieldsValues(values, fields);
    }

    public String[] convertFieldValuesToStrings(T obj) {
        return  reflectionUtil.getFields().stream()
                .map(field -> {
                    Object value = field.getValue(obj);
                    if(value instanceof Date date)
                        return dateParserFormatterUtil.format(date);
                    if(value instanceof LocalDate date)
                        return dateParserFormatterUtil.format(date);
                    if(value instanceof LocalDateTime date)
                        return dateParserFormatterUtil.format(date);
                    if(value instanceof ZonedDateTime date)
                        return dateParserFormatterUtil.format(date);
                    return value.toString();
                })
                .toArray(String[]::new);

    }

    public String[] getHeaders(){
        return reflectionUtil.getHeaders().toArray(String[]::new);
    }
    private T createObjectAndSetFieldsValues(Object[] values, List<SheetField<T>> fields) {
        T obj = reflectionUtil.createInstance();
        for (int i = 0; i < values.length; i++) {
            fields.get(i).setValue(obj, values[i]);
        }
        return obj;
    }

    private Object convertToTypedValue(String value, String typeName) {
        try {
            return switch (typeName) {
                case "string","enum","boolean" -> value;
                case "integer", "int" -> Integer.parseInt(value);
                case "short" -> Short.parseShort(value);
                case "long" -> Long.parseLong(value);
                case "double" -> Double.parseDouble(value.replace(",", "."));
                case "localdate" -> dateParserFormatterUtil.parseToLocalDate(value);
                case "localdatetime" -> dateParserFormatterUtil.parseToLocalDateTime(value);
                case "zoneddatetime" -> dateParserFormatterUtil.parseToZonedDateTime(value);
                case "date" -> convertToDate(value);
                default -> throw new ExcelValidationException("Unsupported typeName: " + typeName);
            };
        }catch (RuntimeException ex){
            throw new ExcelValidationException("Unexpected or Invalid cell value {"+value+"}  ");
        }
    }

    private Date convertToDate(String value) {
        try {
            return dateParserFormatterUtil.parseToDate(value);
        } catch (ParseException e) {
            throw new ExcelValidationException("Invalid date format");
        }
    }

}
