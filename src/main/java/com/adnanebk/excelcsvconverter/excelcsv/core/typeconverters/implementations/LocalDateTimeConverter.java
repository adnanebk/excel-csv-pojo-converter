package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class LocalDateTimeConverter implements Converter<LocalDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    @Override
    public LocalDateTime convertToFieldValue(String cellValue) {
        return LocalDateTime.parse(cellValue,FORMATTER);
    }

    @Override
    public String convertToCellValue(LocalDateTime fieldValue) {
        return FORMATTER.format(fieldValue);
    }
}
