package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

public interface CellConverter<T>  {

    T convertToFieldValue(String cellValue);

}
