package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.FieldConverter;

public class FieldConverterImp<T>  implements Converter<T> {

    private final FieldConverter<T> fieldConverter;

    public FieldConverterImp(FieldConverter<T> fieldConverter) {
        this.fieldConverter = fieldConverter;
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        throw new UnsupportedOperationException("Unsupported operation. cannot convert to field value, use cellConverter instead or Converter to support both conversions");
    }

    @Override
    public String convertToCellValue(T fieldValue) {
        return fieldConverter.convertToCellValue(fieldValue);
    }
}
