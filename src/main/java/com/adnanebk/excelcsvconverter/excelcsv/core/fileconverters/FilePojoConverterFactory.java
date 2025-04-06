package com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters;

import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.csv.CsvPojoConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.excel.ExcelPojoConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.excel.ExcelRowsHandler;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;

public abstract class FilePojoConverterFactory {

    private FilePojoConverterFactory() {
    }

    public static <T> FilePojoConverter<T> createExcelConverter(Class<T> type) {
        var reflectionHelper = new ReflectionHelper<>(type);
        var rowsHandler = new ExcelRowsHandler<>(reflectionHelper);
        return new ExcelPojoConverter<>(rowsHandler);
    }

    public static <T> FilePojoConverter<T> createExcelConverter(Class<T> type, ColumnDefinition<?>... columnsDefinitions) {
        var reflectionHelper = new ReflectionHelper<>(type, columnsDefinitions);
        var rowsHandler = new ExcelRowsHandler<>(reflectionHelper);
        return new ExcelPojoConverter<>(rowsHandler);
    }

    public static <T> FilePojoConverter<T> createCsvConverter(Class<T> type, String delimiter) {
        var reflectionHelper = new ReflectionHelper<>(type);
        return new CsvPojoConverter<>(reflectionHelper,delimiter);
    }

    public static <T> FilePojoConverter<T> createCsvConverter(Class<T> type, String delimiter, ColumnDefinition<?>... columnsDefinitions) {
        var reflectionHelper = new ReflectionHelper<>(type, columnsDefinitions);
        return new CsvPojoConverter<>(reflectionHelper, delimiter);
    }
}
