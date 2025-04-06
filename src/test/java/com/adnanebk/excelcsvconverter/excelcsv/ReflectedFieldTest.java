package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectedField;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import com.adnanebk.excelcsvconverter.excelcsv.models.StringConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class ReflectedFieldTest {


    @Test
    void setValue() throws NoSuchFieldException {
        var reflectedField = new ReflectedField<>(Product.class.getDeclaredField("name"), null, 0,"Name");
        Assertions.assertDoesNotThrow(() -> reflectedField.setValue("a name", new Product()));
    }

    @Test
    void getValueString() throws NoSuchFieldException {
        var reflectedField = new ReflectedField<>(Product.class.getDeclaredField("name"), new StringConverter(), 0,"Name");
        Product product = new Product();
        product.setName("a name");

        Assertions.assertEquals("a name added text", reflectedField.getValue(product));
        Assertions.assertEquals("string", reflectedField.getTypeName());
        reflectedField.setValue("text", product);
        Assertions.assertEquals("text added", product.getName());

    }

    @Test
    void getValueDate() throws NoSuchFieldException {
        var reflectedField = new ReflectedField<>(Product.class.getDeclaredField("updatedDate"), null, 0,"Updated date");
        Product product = new Product();
        product.setUpdatedDate(LocalDate.now());

        Assertions.assertEquals(LocalDate.now(), reflectedField.getValue(product));
        Assertions.assertEquals("localdate", reflectedField.getTypeName());
    }



}

