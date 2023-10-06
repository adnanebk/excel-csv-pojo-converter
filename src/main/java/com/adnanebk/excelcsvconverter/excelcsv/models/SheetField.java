package com.adnanebk.excelcsvconverter.excelcsv.models;

import java.util.function.BiConsumer;
import java.util.function.Function;

public record SheetField<T>(String typeName, String title, Function<T,Object> getter, BiConsumer<T,Object> setter, int colIndex) {
    public Object getValue(T obj) {
        return getter.apply(obj);
    }

    public void setValue(T obj,Object value){
            if(value==null || value.toString().isEmpty())
                return;
        setter.accept(obj, value);
    }


}
