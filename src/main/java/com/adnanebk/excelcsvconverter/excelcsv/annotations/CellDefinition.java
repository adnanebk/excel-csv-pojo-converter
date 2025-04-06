package com.adnanebk.excelcsvconverter.excelcsv.annotations;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.CellConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.Converter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.EnumConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.FieldConverter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CellDefinition {
    String title() default "";

    int value();

    Class<? extends Converter> converter() default Converter.class;
    Class<? extends CellConverter> cellConverter() default CellConverter.class;
    Class<? extends FieldConverter> fieldConverter() default FieldConverter.class;
    Class<? extends EnumConverter> enumConverter() default EnumConverter.class;

}
