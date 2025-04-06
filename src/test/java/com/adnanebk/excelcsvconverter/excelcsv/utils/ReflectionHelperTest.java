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
        assertEquals(12,fields.size());

        assertEquals(0,fields.get(0).getCellIndex());
        assertEquals("string",fields.get(0).getTypeName());

        assertEquals(1,fields.get(1).getCellIndex());
        assertEquals("number",fields.get(1).getTypeName());

        assertEquals(2,fields.get(2).getCellIndex());
        assertEquals("number",fields.get(2).getTypeName());

        assertEquals("number",fields.get(3).getTypeName());

        assertEquals("boolean",fields.get(4).getTypeName());

        assertEquals("enum",fields.get(10).getTypeName());


        assertEquals(5,fields.get(5).getCellIndex());
    }


    @Test
    void createInstance() {
        var product = reflectionHelper.createInstance();
        assertNotNull(product);
        assertEquals(0,product.getPrice());
    }


}