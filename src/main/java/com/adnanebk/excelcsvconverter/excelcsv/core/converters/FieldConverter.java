package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

public interface FieldConverter<T>{

    String convertToCellValue(T fieldValue);

}
