package com.adnanebk.excelcsvconverter.excelcsv.core.reflection;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.ConverterFactory;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ReflectionHelper<T> {
    private final List<ReflectedField<?>> fields;
    private final MethodHandle constructorHandler;

    public ReflectionHelper(Class<T> type) {
        constructorHandler = createConstructorHandler(type);
        fields = createFields(type);
    }

    public ReflectionHelper(Class<T> classType, ColumnDefinition<?>[] columnsDefinitions) {
        constructorHandler = createConstructorHandler(classType);
        fields = createFields(classType, columnsDefinitions);
    }

    public T createInstance() {
         try {
             return (T) constructorHandler.invoke();
            } catch (Throwable e) {
                throw new ReflectionException(e.getMessage());
            }
    }
    public List<ReflectedField<?>> getFields() {
        return fields;
    }

    private MethodHandle createConstructorHandler(Class<T> classType) {
        try {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup();
        return lookup.findConstructor(classType, MethodType.methodType(void.class));
        }
         catch (IllegalAccessException | NoSuchMethodException e) {
                throw new ReflectionException(e.getMessage());
            }
    }

    private List<ReflectedField<?>> createFields(Class<T> classType) {
        return Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .sorted(Comparator.comparing(field -> field.getDeclaredAnnotation(CellDefinition.class).value()))
                .map(field -> {
                   var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                   String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                                  .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                   return new ReflectedField<>(field, ConverterFactory.createConverter(field, cellDefinition), cellDefinition.value(),title);
                }).collect(Collectors.toList());
    }
    private List<ReflectedField<?>> createFields(Class<T> classType, ColumnDefinition<?>[] columnsDefinitions) {
        Arrays.sort(columnsDefinitions,Comparator.comparing(ColumnDefinition::getColumnIndex));
        return Arrays.stream(columnsDefinitions).map(cd->{
            try {
                Field field = classType.getDeclaredField(cd.getFieldName());
                if (cd.getConverter() == null)
                    cd.setConverter(ConverterFactory.createConverter(field),field.getType());
                if(!cd.getClassType().equals(field.getType()))
                    throw new ReflectionException(String.format("The converter of the field %s is not compatible with its type %s",cd.getFieldName(),field.getType().getSimpleName()));
                return new ReflectedField<>(field, cd.getConverter(), cd.getColumnIndex(),cd.getTitle());
            } catch (NoSuchFieldException e) {
                throw new ReflectionException("the specified field name '" + cd.getFieldName() + "' is not found in the class '"+ classType.getSimpleName()+"'");
            }
        }).collect(Collectors.toList());
    }

    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0)) + "";
        String remaining = word.substring(1);
        return firstChar + (remaining.toLowerCase().equals(remaining) ? remaining :
                remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

}

