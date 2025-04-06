package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;

public class StringConverter implements Converter<String> {

    @Override
    public String convertToCellValue(String fieldValue) {
        return fieldValue + " added text";
    }

    @Override
    public String convertToFieldValue(String cellValue) {
        return cellValue + " added";
    }
}
