package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters;

public interface FieldConverter<T>{

    String convertToCellValue(T fieldValue);

}
