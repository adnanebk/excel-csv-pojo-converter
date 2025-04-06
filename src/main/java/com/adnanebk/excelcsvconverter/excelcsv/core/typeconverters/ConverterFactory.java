package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters;

import com.adnanebk.excelcsvconverter.excelcsv.annotations.CellDefinition;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.implementations.*;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;

public abstract class ConverterFactory {

    private ConverterFactory() {
    }

    public  static Converter<?> createConverter(Field field, CellDefinition cellDefinition) {
        try {
            if (isClassImplementation(cellDefinition.converter()))
                return cellDefinition.converter().getDeclaredConstructor().newInstance();
            if (isClassImplementation(cellDefinition.fieldConverter()))
                return new FieldConverterImp<>(cellDefinition.fieldConverter().getDeclaredConstructor().newInstance());
            if (isClassImplementation(cellDefinition.cellConverter()))
                return new CellConverterImp<>(cellDefinition.cellConverter().getDeclaredConstructor().newInstance());
            if (isClassImplementation(cellDefinition.enumConverter()))
                return new EnumConverterImp<>(field.getType(),
                        cellDefinition.enumConverter().getDeclaredConstructor().newInstance());
            return createConverter(field);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException |
                 IllegalAccessException e) {
            throw new ReflectionException(e.getMessage());
        }
    }

    public  static Converter<?> createConverter(Field field) {
        if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class))
            return new BooleanConverterImp();
        if (field.getType().isEnum())
            return new EnumConverterImp<>(field.getType(), new HashMap<>());

        if (field.getType().equals(Date.class)) {
            return new DateConverter();
        }
        if (field.getType().equals(LocalDate.class)) {
            return new LocalDateConverter();
        }
        if (field.getType().equals(LocalDateTime.class)) {
            return new LocalDateTimeConverter();
        }
        if (field.getType().equals(ZonedDateTime.class)) {
            return new ZonedDateConverter();
        }
        if (field.getType().equals(Integer.class) || field.getType().equals(int.class)) {
            return new IntegerConverter();
        }
        if (field.getType().equals(Long.class) || field.getType().equals(long.class)) {
            return new LongConverter();
        }
        if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
            return new DoubleConverter();
        }
        return null;
    }

    private static boolean isClassImplementation(Class<?> type) {
        return !type.isInterface();
    }
}
