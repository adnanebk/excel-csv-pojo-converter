package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.utils.DateParserFormatter;

import java.time.LocalDateTime;


public class LocalDateTimeConverter implements Converter<LocalDateTime> {

    private final DateParserFormatter dateParserFormatter;

    public LocalDateTimeConverter(DateParserFormatter dateParserFormatter) {
        this.dateParserFormatter = dateParserFormatter;
    }

    @Override
    public LocalDateTime convertToFieldValue(String cellValue) {
        return dateParserFormatter.parseToLocalDateTime(cellValue);
    }

    @Override
    public String convertToCellValue(LocalDateTime fieldValue) {
        return dateParserFormatter.format(fieldValue);
    }
}
