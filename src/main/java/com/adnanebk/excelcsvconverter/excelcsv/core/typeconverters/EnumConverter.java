package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters;

import java.util.Map;

public interface EnumConverter<T extends Enum<T>> {
   Map<T,String> convert();

    }
