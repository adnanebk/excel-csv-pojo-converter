package com.adnanebk.excelcsvconverter.excelcsv.core.converters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.converters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.converters.FieldConverter;

public class FieldConverterImp<T>  implements Converter<T> {

    private final FieldConverter<T> fieldConverter;

    public FieldConverterImp(FieldConverter<T> fieldConverter) {
        this.fieldConverter = fieldConverter;
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        throw new UnsupportedOperationException("Not supported operation. cannot convert to field value, use Converter interface to support both conversions");
    }

    @Override
    public String convertToCellValue(T fieldValue) {
        return fieldConverter.convertToCellValue(fieldValue);
    }
}
