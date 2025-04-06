package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.EnumConverter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ConverterException;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EnumConverterImp<T extends Enum<T>> implements Converter<T> {
    private final EnumMap<T,String> enumToCellValue;
    private final  Map<String,T> cellValueToEnum;

    public EnumConverterImp(Class<?> type, Map<?,String> map) {
        if(!type.isEnum())
            throw new ConverterException("Can't convert " + type + " to Enum");
        Class<T> enumType = (Class<T>) type;
       enumToCellValue = new EnumMap<>(enumType);
       cellValueToEnum = new HashMap<>();
        for(var entry : map.entrySet()){
            enumToCellValue.put(enumType.cast(entry.getKey()),entry.getValue());
            cellValueToEnum.put(entry.getValue().toUpperCase(), enumType.cast(entry.getKey()));
        }
        for (var constant : enumType.getEnumConstants()) {
            enumToCellValue.putIfAbsent(constant, constant.toString());
            cellValueToEnum.putIfAbsent(constant.toString(), constant);
        }
    }

    public EnumConverterImp(Class<?> type, EnumConverter<?> enumConverter) {
        this(type,enumConverter.convert());
    }


    @Override
    public String convertToCellValue(T fieldValue) {
        return enumToCellValue.getOrDefault(fieldValue,"");
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        return cellValueToEnum.get(cellValue.toUpperCase());
    }
}
