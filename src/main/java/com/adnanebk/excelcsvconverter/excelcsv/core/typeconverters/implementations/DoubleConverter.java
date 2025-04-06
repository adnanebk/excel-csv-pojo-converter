package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;


public class DoubleConverter implements Converter<Double> {

    @Override
    public Double convertToFieldValue(String cellValue) {
        return Double.parseDouble(cellValue.replace(",","."));
    }

    @Override
    public String convertToCellValue(Double fieldValue) {
        return fieldValue.toString();
    }
}
