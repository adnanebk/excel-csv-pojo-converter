package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.ConverterFactory;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ReflectionHelper<T> {
    private final List<ReflectedField<?>> fields = new ArrayList<>();
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionHelper(Class<T> type) {
        classType = type;
        defaultConstructor = getDefaultConstructor();
        this.setFieldsAndTitles();
    }

    public ReflectionHelper(Class<T> type, ColumnDefinition<?>[] columnsDefinitions) {
        classType = type;
        defaultConstructor = getDefaultConstructor();
        Arrays.sort(columnsDefinitions,Comparator.comparing(ColumnDefinition::getColumnIndex));
        for (var cd : columnsDefinitions) {
            try {
                Field field = type.getDeclaredField(cd.getFieldName());
                if (cd.getConverter() == null)
                    cd.setConverter(ConverterFactory.createConverter(field),field.getType());
                if(!cd.getClassType().equals(field.getType()))
                    throw new ReflectionException(String.format("The converter of the field %s is not compatible with its type %s",cd.getFieldName(),field.getType().getSimpleName()));
                fields.add(new ReflectedField<>(field, cd.getConverter(), cd.getColumnIndex(),cd.getTitle()));
            } catch (NoSuchFieldException e) {
                throw new ReflectionException("the specified field name '" + cd.getFieldName() + "' is not found in the class '"+classType.getSimpleName()+"'");
            }
        }
    }

    public List<ReflectedField<?>> getFields() {
        return fields;
    }


    public T createInstance() {
        try {
            return defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    private Constructor<T> getDefaultConstructor() {
        try {
            return classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private void setFieldsAndTitles() {
        Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(CellDefinition.class).value()))
                .forEach(field -> {
                   var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                   String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                                  .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                   fields.add(new ReflectedField<>(field, ConverterFactory.createConverter(field, cellDefinition), cellDefinition.value(),title));
                });
    }



    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0)) + "";
        String remaining = word.substring(1);
        return firstChar + (remaining.toLowerCase().equals(remaining) ? remaining :
                remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

}

