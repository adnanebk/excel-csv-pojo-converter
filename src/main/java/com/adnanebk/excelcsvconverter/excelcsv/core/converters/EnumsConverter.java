package com.adnanebk.excelcsvconverter.excelcsv.core.converters;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class EnumsConverter<T extends Enum<T>> implements Converter<T> {

    private final EnumMap<T,String> enumToCellValue;
    private final  Map<String,T> cellValueToEnum;

    public EnumsConverter(Class<?> type,Map<?,String> map) {
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


    @Override
    public String convertToCellValue(T fieldValue) {
        return enumToCellValue.getOrDefault(fieldValue,"");
    }

    @Override
    public T convertToFieldValue(String cellValue) {
        return cellValueToEnum.get(cellValue.toUpperCase());
    }
}
