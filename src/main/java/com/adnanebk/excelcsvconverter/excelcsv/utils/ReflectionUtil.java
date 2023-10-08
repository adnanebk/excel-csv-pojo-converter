package com.adnanebk.excelcsvconverter.excelcsv.utils;


import com.adnanebk.excelcsvconverter.excelcsv.annotations.*;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;
import com.adnanebk.excelcsvconverter.excelcsv.models.SheetField;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ReflectionUtil<T> {
    private final List<SheetField<T>> fields;
    private final Class<T> classType;
    private final Constructor<T> defaultConstructor;

    public ReflectionUtil(Class<T> type) {
        classType = type;
        defaultConstructor=getDefaultConstructor();
        fields = getSheetInfo().filter(SheetDefinition::includeAllFields)
                .map(info->getAllFields())
                .orElseGet(this::getAnnotatedFields);
    }
    public List<SheetField<T>> getFields() {
        return fields;
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

    private List<SheetField<T>> getAnnotatedFields() {
        return Arrays.stream(classType.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CellDefinition.class))
                .map(field -> {
                    var cellDefinition = field.getDeclaredAnnotation(CellDefinition.class);
                    String title = Optional.of(cellDefinition.title()).filter(s -> !s.isEmpty())
                                           .orElseGet(() -> camelCaseWordsToTitleWords(field.getName()));
                    return buildField(title, cellDefinition.value(), field);
                })
                .sorted(Comparator.comparing(SheetField::cellIndex))
                .toList();
    }
    private List<SheetField<T>> getAllFields() {
        var titles = classType.getAnnotation(SheetDefinition.class).titles();
        int index=0;
        List<SheetField<T>> sheetFields = new ArrayList<>();
        for (var field : classType.getDeclaredFields()) {
            if (!field.isAnnotationPresent(IgnoreCell.class)) {
                String title=index < titles.length ? titles[index]:camelCaseWordsToTitleWords(field.getName());
                sheetFields.add(buildField(title, index, field));
            }
            index++;
        }
        return sheetFields;
    }
    private Function<T,Object> getFieldGetter(String fieldName, Class<?> fieldType, Map<Object, Object> enumsMapper, String falseValue, String trueValue) {
        try {
            var getterMethod=  classType.getDeclaredMethod((fieldType.equals(boolean.class) ? "is" : "get") + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1));
            boolean isBoolean = fieldType.equals(boolean.class) || fieldType.equals(Boolean.class);
            boolean isEnum=fieldType.isEnum();
            return obj-> {
                 try {
                     Object value=getterMethod.invoke(obj);
                     if (isEnum)
                         return enumsMapper.get(value);
                     else if (isBoolean)
                          return ((boolean) value) ? trueValue : falseValue;
                     return value;
                 } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                     throw new ReflectionException(e.getMessage());
                 }
             };
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException("No getter found");
        }
    }
    private BiConsumer<T,Object> getFieldSetter(String fieldName, Class<?> fieldType, Map<Object, Object> enumsMapper, String trueValue) {
        try {
            var setterMethod = classType.getDeclaredMethod("set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1), fieldType);
            boolean isBoolean = fieldType.equals(boolean.class) || fieldType.equals(Boolean.class);
            boolean isEnum=fieldType.isEnum();
            return (obj,value)-> {
                 try {
                     if(isEnum)
                         setterMethod.invoke(obj,enumsMapper.get(value));
                     else if (isBoolean)
                         setterMethod.invoke(obj,String.valueOf(value).equalsIgnoreCase(trueValue));
                     else setterMethod.invoke(obj,value);
                 } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException | ClassCastException e) {
                     throw new ReflectionException(e.getMessage());
                 }
             };
        } catch (NoSuchMethodException ex) {
            throw new ReflectionException("No setter found");
        }
    }
    private SheetField<T> buildField(String title, int colIndex, Field field) {
        String fieldTypeName = field.getType().isEnum() ? "enum" : field.getType().getSimpleName().toLowerCase();
        String fieldName = field.getName();
        var enumsMapper = createEnumsMapper(field);
        String falseValue= getBooleanFalseValue(field);
        String trueValue= getBooleanTrueValue(field);
        var getter=getFieldGetter(fieldName, field.getType(),enumsMapper,falseValue,trueValue);
        var setter=getFieldSetter(fieldName, field.getType(),enumsMapper,trueValue);
        return new SheetField<>(fieldTypeName,title,getter,setter,colIndex);
    }

    private  Map<Object,Object> createEnumsMapper(Field field) {
        Map<Object, Object> enumsMapper = new HashMap<>();
        if (!field.getType().isEnum())
            return enumsMapper;
        for(var entry : getEnumsMapper(field).entrySet()){
            enumsMapper.put(entry.getKey(),entry.getValue());
            enumsMapper.put(entry.getValue(),entry.getKey());
        }
        var constants = field.getType().asSubclass(Enum.class).getEnumConstants();
        for (var constant : Arrays.stream(constants).filter(constant -> !enumsMapper.containsKey(constant)).toList()) {
            enumsMapper.put(constant, constant.toString());
            enumsMapper.put(constant.toString(), constant);
        }
        return enumsMapper;
    }

    private Map<?, ?> getEnumsMapper( Field field) {
        var enumsAnnotation = field.getDeclaredAnnotation(CellEnum.class);
        if(enumsAnnotation==null)
            return new HashMap<>();
        var enumMapperMethod = enumsAnnotation.enumsMapperMethod();
        try {
            Method method = field.getDeclaringClass().getDeclaredMethod(enumMapperMethod);
            method.setAccessible(true);
            if(method.invoke(createInstance()) instanceof Map<?, ?> enumsMapper
                    && enumsMapper.entrySet().stream().allMatch(entry -> field.getType().isInstance(entry.getKey())
                    && entry.getValue() instanceof String))
                return enumsMapper;
            else throw new ReflectionException("expecting a method that return a map of typeName  Map<"+field.getName()+",String>");
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new ReflectionException("no method found for the argument enumsMapMethod,you must create a method that returns a map");
        }
    }
    private String getBooleanTrueValue(Field field){
       return Optional.ofNullable(field.getDeclaredAnnotation(CellBoolean.class))
                .map(CellBoolean::trueValue).orElse("true");
    }
    private String getBooleanFalseValue(Field field){
        return Optional.ofNullable(field.getDeclaredAnnotation(CellBoolean.class))
                .map(CellBoolean::falseValue).orElse("false");
    }

    private String camelCaseWordsToTitleWords(String word) {
        String firstChar = Character.toUpperCase(word.charAt(0))+"";
        String remaining = word.substring(1);
        return firstChar + (remaining.toLowerCase().equals(remaining)?remaining:
                remaining.replaceAll("([a-z])([A-Z]+)", "$1 $2").toLowerCase());
    }

}

