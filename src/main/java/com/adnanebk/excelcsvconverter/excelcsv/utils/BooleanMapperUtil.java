package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellBoolean;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BooleanMapperUtil {

    private BooleanMapperUtil() {
    }
    private static final Map<Field,BooleanValues> mapper = new HashMap<>();


    public static void createNewMapping(Field field){
        String trueValue= Optional.ofNullable(field.getDeclaredAnnotation(CellBoolean.class))
                .map(CellBoolean::trueValue).orElse("true");
        String falseValue = Optional.ofNullable(field.getDeclaredAnnotation(CellBoolean.class))
                .map(CellBoolean::falseValue).orElse("false");
        mapper.put(field,new BooleanValues(trueValue,falseValue));
    }
    static boolean getBoolean(Field field,String value){
        return mapper.get(field).trueValue.equalsIgnoreCase(value);
    }
    static String getValue(Field field,boolean val){
            return val ? mapper.get(field).trueValue : mapper.get(field).falseValue;
    }
    private record BooleanValues(String trueValue,String  falseValue){}
}
