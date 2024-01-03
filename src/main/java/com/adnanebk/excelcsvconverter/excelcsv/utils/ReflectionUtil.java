package com.adnanebk.excelcsvconverter.excelcsv.utils;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.*;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.excelcsv.models.SheetField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReflectionUtil<T> {
    private  final List<SheetField<T>> fields = new ArrayList<>();
    private  final List<String> headers = new ArrayList<>();
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionUtil(Class<T> type) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
        this.setFieldsAndTitles();
    }
    public List<SheetField<T>> getFields() {
        return fields;
    }
    public List<String> getHeaders() {
        return headers;
    }

    public T createInstance(){
        try{
            return defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public Optional<SheetDefinition> getSheetInfo() {
        return Optional.ofNullable(classType.getAnnotation(SheetDefinition.class));
    }

    private Constructor<T> getDefaultConstructor() {
        try {
            return classType.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ReflectionException("No default constructor found");
        }
    }

    private void setFieldsAndTitles(){
        boolean hasIncludeAllFields =  getSheetInfo().filter(SheetDefinition::includeAllFields).isPresent();
        if(hasIncludeAllFields)
           this.createAllFieldsAndTitles();
        else this.createAnnotatedFieldsAndTitles();
    }
    private void createAnnotatedFieldsAndTitles() {
         Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .sorted(Comparator.comparing(field -> {
                    var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                    return cellDefinition.value();
                }))
                .forEach(field->{
                    var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                    var sheetField = buildField(cellDefinition.value(), field);
                    String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                                  .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                    fields.add(sheetField);
                    headers.add(title);
                });
    }
    private void createAllFieldsAndTitles() {
        var titles = classType.getAnnotation(SheetDefinition.class).titles();
        int index=0;
        for (var field : classType.getDeclaredFields()) {
            if (!field.isAnnotationPresent(IgnoreCell.class)) {
                String title=index < titles.length ? titles[index]:camelCaseWordsToTitleWords(field.getName());
                headers.add(title);
                fields.add(buildField(index, field));
            }
            index++;
        }
    }
    private SheetField<T> buildField(int colIndex, Field field) {
        String fieldTypeName = field.getType().isEnum() ? "enum" : field.getType().getSimpleName().toLowerCase();
        boolean isBoolean = field.getType().equals(boolean.class) || field.getType().equals(Boolean.class);
        boolean isEnum=field.getType().isEnum();
        if(isEnum)
          EnumsMapperUtil.createNewMapping(field,createInstance());
        else if(isBoolean)
            BooleanMapperUtil.createBooleanValues(field);

        var getter=getFieldGetter(field,isEnum,isBoolean);
        var setter=getFieldSetter(field,isEnum,isBoolean);
        return new SheetField<>(fieldTypeName,getter,setter,colIndex);
    }
    private Function<T,Object> getFieldGetter(Field field, boolean isEnum, boolean isBoolean) {
        try {
            var fieldType = field.getType();
            var fieldName = field.getName();
            var getterMethod=  classType.getDeclaredMethod((fieldType.equals(boolean.class) ? "is" : "get") + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1));
            return obj-> {
                 try {
                     Object value=getterMethod.invoke(obj);
                     if (isEnum)
                         return EnumsMapperUtil.getValue(field,value);
                     else if (isBoolean)
                          return BooleanMapperUtil.getValue(field,(boolean) value);
                     return value;
                 } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                     throw new ReflectionException(e.getMessage());
                 }
             };
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException("No getter found");
        }
    }
    private BiConsumer<T,Object> getFieldSetter(Field field, boolean isEnum, boolean isBoolean) {
        try {
            var fieldType = field.getType();
            var fieldName = field.getName();
            var setterMethod = classType.getDeclaredMethod("set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1), fieldType);
            return (obj,value)-> {
                 try {
                     if(isEnum)
                         setterMethod.invoke(obj,EnumsMapperUtil.getValue(field,value));
                     else if (isBoolean)
                         setterMethod.invoke(obj,BooleanMapperUtil.getBoolean(field,String.valueOf(value)));
                     else setterMethod.invoke(obj,value);
                 } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | ClassCastException e) {
                     throw new ReflectionException(e.getMessage());
                 }
             };
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException("No setter found");
        }
    }

    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0))+"";
        String remaining = word.substring(1);
        return firstChar + (remaining.toLowerCase().equals(remaining)?remaining:
                remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

}

