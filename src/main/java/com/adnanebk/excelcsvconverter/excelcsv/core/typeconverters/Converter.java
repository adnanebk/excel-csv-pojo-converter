package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters;

public interface Converter<T>  {

    T convertToFieldValue(String cellValue);

    String convertToCellValue(T fieldValue);

}
