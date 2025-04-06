package com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public interface FilePojoConverter<T> {


    Stream<T> toStream(InputStream inputStream);

    ByteArrayInputStream toByteArrayInputStream(List<T> list);
}
