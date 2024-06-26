package com.adnanebk.excelcsvconverter.excelcsv.core.heplers;


import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.core.rows_handlers.CsvRowsHandler;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


public class CsvHelper<T> {

    public static final char QUOTE_CHARACTER = ICSVWriter.NO_QUOTE_CHARACTER;
    public static final char ESCAPE_CHARACTER = '\t';
    public static final String LINE_END = "\n";
    public  final String delimiter;
    private final String[] headers;
    private final CsvRowsHandler<T> rowsHandler;

    private CsvHelper(CsvRowsHandler<T> rowsHandler, String delimiter, String[] headers) {
        this.rowsHandler = rowsHandler;
        this.delimiter =delimiter;
        this.headers= headers;
    }

    public static <T> CsvHelper<T> create(Class<T> type,String delimiter) {
        return create(type,delimiter,null);
    }
    public static <T> CsvHelper<T> create(Class<T> type,String delimiter,String[] headers) {
        var reflectionHelper = new ReflectionHelper<>(type);
        var rowsHandler = new CsvRowsHandler<>(reflectionHelper);
        return new CsvHelper<>(rowsHandler,delimiter,Optional.ofNullable(headers)
                .orElse(reflectionHelper.getHeaders().toArray(String[]::new)));
    }

    public Stream<T> toStream(InputStream inputStream) {
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            return br.lines().skip(1)
                    .map(row -> this.rowsHandler.convertToObject(row,delimiter,QUOTE_CHARACTER));
    }

    public ByteArrayInputStream toCsv(List<T> list) throws IOException {
        StringWriter stringWriter=new StringWriter();
        try(CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), QUOTE_CHARACTER, ESCAPE_CHARACTER, LINE_END)) {
            List<String[]> data = new LinkedList<>();
            data.add(headers);
            for (T obj : list) {
                data.add(rowsHandler.convertFieldValuesToStrings(obj));
            }
            csvWriter.writeAll(data);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        }
    }


}