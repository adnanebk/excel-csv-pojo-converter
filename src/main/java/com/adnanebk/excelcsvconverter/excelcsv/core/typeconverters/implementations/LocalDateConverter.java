package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


public class LocalDateConverter implements Converter<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;


    @Override
    public LocalDate convertToFieldValue(String cellValue) {
        return LocalDate.parse(cellValue, FORMATTER);
    }

    @Override
    public String convertToCellValue(LocalDate fieldValue) {
        return FORMATTER.format(fieldValue);
    }
}
