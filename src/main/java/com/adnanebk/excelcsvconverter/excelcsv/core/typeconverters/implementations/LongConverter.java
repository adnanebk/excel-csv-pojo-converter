package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;


public class LongConverter implements Converter<Long> {

    @Override
    public Long convertToFieldValue(String cellValue) {
        return Long.parseLong(cellValue);
    }

    @Override
    public String convertToCellValue(Long fieldValue) {
        return fieldValue.toString();
    }
}
