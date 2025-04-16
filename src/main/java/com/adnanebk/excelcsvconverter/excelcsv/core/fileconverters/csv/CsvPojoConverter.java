package com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.csv;


import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.FilePojoConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectedField;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;


public class CsvPojoConverter<T> implements FilePojoConverter<T> {

    public static final char QUOTE_CHARACTER = ICSVWriter.NO_QUOTE_CHARACTER;
    public static final char ESCAPE_CHARACTER = '\t';
    public static final String LINE_END = "\n";
    public final String delimiter;
    private final String[] headers;
    private final ReflectionHelper<T> reflectionHelper;

    public CsvPojoConverter(ReflectionHelper<T> reflectionHelper, String delimiter) {
        this.reflectionHelper = reflectionHelper;
        this.delimiter = delimiter;
        this.headers = reflectionHelper.getFields().stream().map(ReflectedField::getTitle).toArray(String[]::new);
    }


    @Override
    public Stream<T> toStream(InputStream inputStream) {
        try (CSVReader reader = new CSVReaderBuilder(
                new BufferedReader(new InputStreamReader(inputStream)))
                .withCSVParser(new CSVParserBuilder()
                        .withSeparator(delimiter.charAt(0))
                        .withEscapeChar(ESCAPE_CHARACTER)
                        .withQuoteChar(QUOTE_CHARACTER)
                        .build())
                .build()) {
            return reader.readAll().stream()
                    .skip(1) // Skip header
                    .map(this::convertToObject);
        } catch (IOException | CsvException  e) {
            throw new SheetValidationException(e.getMessage());
        }
    }

    @Override
    public ByteArrayInputStream toByteArrayInputStream(List<T> list) {
        StringWriter stringWriter = new StringWriter();
        try (CSVWriter csvWriter = new CSVWriter(stringWriter, delimiter.charAt(0), QUOTE_CHARACTER, ESCAPE_CHARACTER, LINE_END)) {
            List<String[]> lines = new LinkedList<>();
            lines.add(headers);
            list.forEach(obj->lines.add(this.convertToLine(obj)));
            csvWriter.writeAll(lines);
            return new ByteArrayInputStream(stringWriter.toString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new SheetValidationException(e.getMessage());
        }
    }

    private T convertToObject(String[] columns) {
        var fields = reflectionHelper.getFields();
        T obj = reflectionHelper.createInstance();
        for (int i = 0; i < Math.min(columns.length, fields.size()); i++) {
            var field = fields.get(i);
            String cellValue = columns[i];
            try {
                if(!cellValue.isEmpty())
                    field.setValue(cellValue, obj);
            } catch (IllegalArgumentException e) {
                throw new SheetValidationException(String.format("Cannot convert the cell value %s to the field %s", cellValue,field.getName()));
            } catch (DateTimeException e) {
                throw new SheetValidationException(String.format("Invalid or unsupported date pattern for cell value : %s ,you should create a converter for the field %s", cellValue,field.getName()));
            }
        }
        return obj;
    }

    private String[] convertToLine(T obj) {
        return reflectionHelper.getFields().stream()
                .map(field -> field.getValueAsString(obj))
                .toArray(String[]::new);
    }

}