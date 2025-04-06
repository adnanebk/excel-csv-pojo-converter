package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters;

public interface CellConverter<T>  {

    T convertToFieldValue(String cellValue);

}
