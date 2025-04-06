package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;


public class ZonedDateConverter implements Converter<ZonedDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_ZONED_DATE_TIME;


    @Override
    public ZonedDateTime convertToFieldValue(String cellValue) {
        return ZonedDateTime.parse(cellValue,FORMATTER);
    }

    @Override
    public String convertToCellValue(ZonedDateTime fieldValue) {
        return FORMATTER.format(fieldValue);
    }
}
