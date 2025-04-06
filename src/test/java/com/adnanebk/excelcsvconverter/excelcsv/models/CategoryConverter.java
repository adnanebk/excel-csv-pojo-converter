package com.adnanebk.excelcsvconverter.excelcsv.models;

import com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters.EnumConverter;

import java.util.Map;

public class CategoryConverter implements EnumConverter<Category> {
    Map<Category,String> map =  Map.of(Category.A,"aa",
            Category.B,"bb",
            Category.C,"cc"
    );

    @Override
    public Map<Category, String> convert() {
        return map;
    }
}
