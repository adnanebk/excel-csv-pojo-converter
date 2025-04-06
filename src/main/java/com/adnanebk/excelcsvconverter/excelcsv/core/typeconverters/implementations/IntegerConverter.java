package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;


public class IntegerConverter implements Converter<Integer> {

    @Override
    public Integer convertToFieldValue(String cellValue) {
        return Integer.parseInt(cellValue);
    }

    @Override
    public String convertToCellValue(Integer fieldValue) {
        return fieldValue.toString();
    }
}
