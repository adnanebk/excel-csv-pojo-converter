package com.adnanebk.excelcsvconverter.excelcsv.core;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.CellConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.EnumConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.FieldConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations.CellConverterImp;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations.EnumConverterImp;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations.FieldConverterImp;


public final class ColumnDefinitionBuilder {
    private final int columnIndex;
    private final String fieldName;
    private final String title;
    private Converter<?> converter;
    private Class<?> classType;


    public ColumnDefinitionBuilder(int columnIndex, String fieldName, String title) {
        this.columnIndex = columnIndex;
        this.fieldName = fieldName;
        this.title = title;
    }

    public <T>  ColumnDefinition<T> build() {
        return new ColumnDefinition<>(columnIndex, fieldName, title, (Converter<T>) converter,(Class<T>) classType);
    }
    public <T> ColumnDefinitionBuilder withConverter(Class<T> classType, Converter<T> converter) {
        this.classType = classType;
        this.converter = converter;
        return this;
    }

    public <T> ColumnDefinitionBuilder withCellConverter(Class<T> classType, CellConverter<T> cellConverter) {
        this.classType = classType;
        this.converter = new CellConverterImp<>(cellConverter);
        return this;
    }

    public <T> ColumnDefinitionBuilder withFieldConverter(Class<T> classType, FieldConverter<T> fieldConverter) {
        this.classType = classType;
        this.converter = new FieldConverterImp<>(fieldConverter);
        return this;
    }
    public <T extends Enum<T>> ColumnDefinitionBuilder withEnumConverter(Class<T> classType, EnumConverter<T> enumConverter) {
       this.classType = classType;
       this.converter = new EnumConverterImp<>(classType,enumConverter);
       return this;
    }

}
