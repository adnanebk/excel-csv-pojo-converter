package com.adnanebk.excelcsvconverter.excelcsv.core;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;

public class ColumnDefinition<T> {

    private Class<T> classType;
    private final int columnIndex;
    private final String fieldName;
    private final String title;
    private Converter<T> converter;

     ColumnDefinition(int columnIndex, String fieldName, String title, Converter<T> converter,Class<T> classType) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = converter;
        this.classType = classType;
    }

    public int getColumnIndex() {
        return columnIndex;
    }


    public String getFieldName() {
        return fieldName;
    }

    public String getTitle() {
        return title;
    }

    public Converter<T> getConverter() {
        return converter;
    }

    public void setConverter(Converter<?> converter,Class<?> classType) {
        this.classType = (Class<T>) classType;
        this.converter = (Converter<T>) converter;
    }

    public Class<T> getClassType() {
        return classType;
    }
}
