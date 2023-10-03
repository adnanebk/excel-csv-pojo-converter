package com.adnanebk.excelcsvconverter.excelcsv.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SheetDefinition {
    boolean includeAllFields() default false;

    String[] titles() default {};

    String datePattern();

    String dateTimePattern();



}
