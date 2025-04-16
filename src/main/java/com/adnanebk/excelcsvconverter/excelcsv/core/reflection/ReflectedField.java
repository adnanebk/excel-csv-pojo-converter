package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectedField<T> {
    private final Method getter;
    private final Method setter;
    private final int cellIndex;
    private final Converter<T> converter;
    private final String typeName;
    private final String name;
    private final String title;

    public ReflectedField(Field field, Converter<T> converter, int cellIndex,String title) {
        try {
            this.title = title;
            this.converter = converter;
            this.name = field.getName();
            this.getter = field.getDeclaringClass().getDeclaredMethod(getGetterMethodName(field));
            this.setter = field.getDeclaringClass().getDeclaredMethod(getSetterMethodName(field),field.getType());
            this.cellIndex = cellIndex;
            typeName = getTypeName(field);
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("not found all getters and setters");
        }
    }

    public String getValueAsString(Object obj) {
        try {
            Object fieldValue = getter.invoke(obj);
            return converter.convertToCellValue((T) fieldValue);
        } catch (IllegalAccessException | InvocationTargetException | ClassCastException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public void setValue(String cellValue, Object obj) {
        try {
             setter.invoke(obj, converter.convertToFieldValue(cellValue));
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public int getCellIndex() {
        return cellIndex;
    }

    public String getTypeName() {
        return typeName;
    }

    public String getTitle() {
        return title;
    }

    public String getName() {
        return name;
    }
    private String getTypeName(Field field) {
        if (field.getType().isEnum())
            return  "enum";
        else if (isNumericType(field.getType()))
            return  "number";
        return field.getType().getSimpleName().toLowerCase();
    }
    private String getSetterMethodName(Field field) {
        return  "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
    }

    private  String getGetterMethodName(Field field) {
        return (field.getType().equals(boolean.class) ? "is" : "get") + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
    }
    private boolean isNumericType(Class<?> clazz) {
        return clazz == byte.class || clazz == short.class || clazz == int.class ||
                clazz == long.class || clazz == float.class || clazz == double.class ||
                clazz == Byte.class || clazz == Short.class || clazz == Integer.class ||
                clazz == Long.class || clazz == Float.class || clazz == Double.class;
    }


}
