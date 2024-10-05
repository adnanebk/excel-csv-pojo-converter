package com.adnanebk.excelcsvconverter.excelcsv.core;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.CellConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.EnumConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.FieldConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.CellConverterImp;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.EnumConverterImp;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations.FieldConverterImp;


public class ColumnDefinition {

    private int columnIndex;
    private String fieldName;
    private String title;
    private Converter<?> converter;

    private ColumnDefinition(int columnIndex, String fieldName, String title, Converter<?> converter) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
        this.converter = converter;
    }

    public static ColumnDefinition create(int columnIndex, String fieldName, String title) {
        return new ColumnDefinition(columnIndex,fieldName,title,null);

    }
    public static ColumnDefinition create(int columnIndex, String fieldName, String title,Converter<?> converter) {
        return new ColumnDefinition(columnIndex,fieldName,title,converter);
    }

    public static ColumnDefinition createWithCellConverter(int columnIndex, String fieldName, String title, CellConverter<?> cellConverter) {
        return new ColumnDefinition(columnIndex,fieldName,title,new CellConverterImp<>(cellConverter));
    }

    public static ColumnDefinition createWithFieldConverter(int columnIndex, String fieldName, String title, FieldConverter<?> fieldConverter) {
        return new ColumnDefinition(columnIndex,fieldName,title,new FieldConverterImp<>(fieldConverter));
    }

    public static ColumnDefinition   create(int columnIndex, String fieldName, String title, EnumConverter<? extends Enum<?>> converter, Class<? extends Enum<?>> classType) {
        return new ColumnDefinition(columnIndex,fieldName,title,new EnumConverterImp<>(classType,converter));
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

    public Converter<?> getConverter() {
        return converter;
    }

    public void setConverter(Converter<?> converter) {
        this.converter = converter;
    }
    
}
