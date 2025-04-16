package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ConverterException;

public class DefaultConverter<T>  implements Converter<T> {

    private final Class<T> classType;
    private final String fieldName;

    public DefaultConverter(Class<?> classType,String fieldName) {
        this.classType = (Class<T>) classType;
        this.fieldName = fieldName;
    }


    @Override
    public T convertToFieldValue(String cellValue) {
        try {
            return classType.cast(cellValue);
        } catch (ClassCastException ex){
            throw new ConverterException(String.format("the cell value %s cannot be converted to type %s, for the field %s , please use a custom converter",cellValue,classType.getSimpleName(),fieldName));
        }
    }

    @Override
    public String convertToCellValue(T fieldValue) {
        return fieldValue.toString();
    }
}
