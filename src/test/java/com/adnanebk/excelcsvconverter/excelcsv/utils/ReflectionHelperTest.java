package com.adnanebk.excelcsvconverter.excelcsv.utils;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReflectionHelperTest {

    private final ReflectionHelper<Product> reflectionHelper = new ReflectionHelper<>(Product.class);

    @Test
    void getFields() {
        var fields = reflectionHelper.getFields();
        Assertions.assertNotNull(fields);
        Assertions.assertEquals(12,fields.size());

        Assertions.assertEquals(0,fields.get(0).getCellIndex());
        Assertions.assertEquals("string",fields.get(0).getTypeName());

        Assertions.assertEquals(1,fields.get(1).getCellIndex());
        Assertions.assertEquals("number",fields.get(1).getTypeName());

        Assertions.assertEquals(2,fields.get(2).getCellIndex());
        Assertions.assertEquals("number",fields.get(2).getTypeName());

        Assertions.assertEquals("number",fields.get(3).getTypeName());

        Assertions.assertEquals("boolean",fields.get(4).getTypeName());

        Assertions.assertEquals("enum",fields.get(10).getTypeName());


        Assertions.assertEquals(5,fields.get(5).getCellIndex());
    }

    @Test
    void getHeaders() {
        var headers = reflectionHelper.getHeaders();
        Assertions.assertNotNull(headers);
        Assertions.assertEquals(12,headers.size());
        Assertions.assertEquals("Promotion price",headers.get(2));
        Assertions.assertEquals("Units in stock",headers.get(6));
    }

    @Test
    void createInstance() {
        var product = reflectionHelper.createInstance();
        assertNotNull(product);
        assertEquals(0,product.getPrice());
    }

    @Test
    void getSheetInfo() {
        var dateParserFormatter = reflectionHelper.getDateParserFormatter();
        assertNotNull(dateParserFormatter);
    }
}