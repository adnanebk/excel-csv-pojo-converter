package com.adnanebk.excelcsvconverter.excelcsv.core.typeconverters;

import java.util.Map;

public interface EnumConverter<T extends Enum<?>> {
   Map<T,String> convert();

    }
