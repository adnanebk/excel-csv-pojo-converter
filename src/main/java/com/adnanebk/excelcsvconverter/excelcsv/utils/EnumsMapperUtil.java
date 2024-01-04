package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellEnum;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EnumsMapperUtil {

    private static final Map<Field,Map<Object,Object>> mapper = new HashMap<>();

    private EnumsMapperUtil() {
    }
    public static Object getValue(Field field, Object value) {
        return mapper.get(field).get(value);
    }
    public static void createNewMapping(Field field, Object instance) {
        Map<Object, Object> enumsMapper = new HashMap<>();
        for(var entry : getEnumsMapper(field,instance).entrySet()){
            enumsMapper.put(entry.getKey(),entry.getValue());
            enumsMapper.put(entry.getValue().toString().toUpperCase(),entry.getKey());
            enumsMapper.put(entry.getKey().toString().toUpperCase(),entry.getKey());
        }
        var constants = field.getType().asSubclass(Enum.class).getEnumConstants();
        for (var constant : Arrays.stream(constants).filter(constant -> !enumsMapper.containsKey(constant)).toList()) {
            enumsMapper.put(constant, constant.toString());
            enumsMapper.put(constant.toString(), constant);
        }
         mapper.put(field,enumsMapper);
    }
    private static Map<?, ?> getEnumsMapper( Field field,Object instance) {
        var enumsAnnotation = field.getDeclaredAnnotation(CellEnum.class);
        if(enumsAnnotation==null)
            return new HashMap<>();
        var enumMapperMethod = enumsAnnotation.enumsMapperMethod();
        try {
            Method method = field.getDeclaringClass().getDeclaredMethod(enumMapperMethod);
            method.setAccessible(true);
            if(method.invoke(instance) instanceof Map<?, ?> enumsMapper){
                ParameterizedType parameterizedType = (ParameterizedType) method.getGenericReturnType();
                Class<?> keyType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                Class<?> valueType = (Class<?>) parameterizedType.getActualTypeArguments()[1];
                 if(keyType.equals(field.getType()) && valueType.equals(String.class))
                   return enumsMapper;
            }
            throw new ReflectionException("expecting a method that returns a map of type  Map<"+field.getName()+",String>");
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ReflectionException("no method found for the argument enumsMapMethod,you must create a method that returns a map");
        }
    }
}
