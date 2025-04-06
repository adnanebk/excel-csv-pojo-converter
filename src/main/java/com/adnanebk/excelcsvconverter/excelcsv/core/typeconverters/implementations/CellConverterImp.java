package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.CellConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;

public class CellConverterImp<T>  implements Converter<T> {

    private final CellConverter<T> cellConverter;

    public CellConverterImp(CellConverter<T> cellConverter) {
        this.cellConverter = cellConverter;
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        return cellConverter.convertToFieldValue(cellValue);
    }

    @Override
    public String convertToCellValue(T fieldValue) {
        return fieldValue.toString();
    }
}
