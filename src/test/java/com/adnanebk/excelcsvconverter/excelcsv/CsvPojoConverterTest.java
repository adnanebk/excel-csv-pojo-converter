package com.adnanebk.excelcsvconverter.excelcsv;

import com.adnanebk.excelcsvconverter.excelcsv.core.ColumnDefinitionBuilder;
import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.FilePojoConverter;
import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.FilePojoConverterFactory;
import com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.csv.CsvPojoConverter;
import com.adnanebk.excelcsvconverter.excelcsv.models.BooleanConverter;
import com.adnanebk.excelcsvconverter.excelcsv.models.Category;
import com.adnanebk.excelcsvconverter.excelcsv.models.Product;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CsvPojoConverterTest {
    private static FilePojoConverter<Product> csvPojoConverter;
    private static FilePojoConverter<Product> csvPojoConverter2;


     @BeforeAll
     static void setUp() {
        csvPojoConverter = FilePojoConverterFactory.createCsvConverter(Product.class, ";");
        csvPojoConverter2 = FilePojoConverterFactory.createCsvConverter(Product.class, ";",
                new ColumnDefinitionBuilder(0, "name", "Name").build(),
                new ColumnDefinitionBuilder(1, "price", "Price").withCellConverter(long.class, Long::parseLong).build(),
                new ColumnDefinitionBuilder(2, "promoPrice", "Promotion price").build(),
                new ColumnDefinitionBuilder(5, "expired", "Expired").withConverter(Boolean.class,new BooleanConverter()).build(),
                new ColumnDefinitionBuilder(3, "minPrice", "Min price").build(),
                new ColumnDefinitionBuilder(4, "active", "Active").build(),
                new ColumnDefinitionBuilder(6, "unitsInStock", "Units in stock").build(),
                new ColumnDefinitionBuilder(7, "createdDate", "Created date").build(),
                new ColumnDefinitionBuilder(8, "updatedDate", "Updated date").build(),
                new ColumnDefinitionBuilder(9, "zonedDateTime", "Zoned date time").build(),
                new ColumnDefinitionBuilder(10, "category", "Category")
                        .withEnumConverter(Category.class,()->Map.of(Category.A,"aa", Category.B,"bb", Category.C,"cc")).build(),
                new ColumnDefinitionBuilder(11, "localDateTime", "Local date time").build()
        );
    }

    private static List<Product> getProducts() {
        List<Product> productList = new ArrayList<>();
        productList.add(new Product("Product A", 100, 90.5, 80.0, true, false, 50, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B, LocalDateTime.now()));
        productList.add(new Product("Product B", 200, 180.75, 150.0, true, true, 30, new Date(), LocalDate.now(), ZonedDateTime.now(), Category.B,LocalDateTime.now()));
        return productList;
    }
    @ParameterizedTest
    @MethodSource("getAllHelpers")
    @Order(0)
    void toCsv_withValidProductData_shouldReturnCorrectExcel(FilePojoConverter<Product> csvPojoConverter) {
        List<Product> productList = getProducts();
        String destinationPath = "src/test/resources/products.csv";
        File file =new File(destinationPath);
        try (ByteArrayInputStream byteArrayInputStream = csvPojoConverter.toByteArrayInputStream(productList);
             OutputStream outputStream = new FileOutputStream(file)) {
            byteArrayInputStream.transferTo(outputStream);
            BufferedReader reader = new BufferedReader(new FileReader(destinationPath));
            List<String> lines = reader.lines().toList();
            assertNotNull(lines);
            assertEquals(3, lines.size());
            assertTrue(lines.get(0).contains("Name;Price;Promotion price;Min price;Active;Expired;Units in stock;Created date;Updated date;Zoned date time;Category;Local date time"));
            assertTrue(lines.get(1).contains("Product A;100;90.5;80.0;true;No;50"));
            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }


    @ParameterizedTest
    @MethodSource("getAllHelpers")
    @Order(1)
    void toList_withValidCsvFile_shouldReturnCorrectProductList(CsvPojoConverter<Product> csvPojoConverter) throws IOException {
        String destinationPath = "src/test/resources/products.csv";
        // Read the file as an InputStream
        try (InputStream inputStream = Files.newInputStream(new File(destinationPath).toPath())) {
            // Create a MockMultipartFile
            List<Product> result = csvPojoConverter.toStream(inputStream).toList();

            assertEquals("Product A", result.get(0).getName());
            assertEquals(100, result.get(0).getPrice());
            assertEquals(90.5, result.get(0).getPromoPrice()); // Delta for double comparison
            assertTrue(result.get(0).isActive());
            assertFalse(result.get(0).getExpired());
            assertEquals(50, result.get(0).getUnitsInStock());
            assertEquals("Product B", result.get(1).getName());
            assertEquals(200, result.get(1).getPrice());
            assertEquals(180.75, result.get(1).getPromoPrice()); // Delta for double comparison
            assertTrue(result.get(1).isActive());
            assertTrue(result.get(1).getExpired());
            assertEquals(30, result.get(1).getUnitsInStock());
            assertNotNull(result.get(1).getCreatedDate()); // Assuming it's not null in the Excel file
            assertTrue(LocalDate.now().isEqual(result.get(1).getUpdatedDate())); // Assuming it's not null in the Excel file
            assertNotNull(result.get(1).getZonedDateTime()); // Assuming it's not null in the Excel file
            assertSame(Category.B, result.get(1).getCategory()); // Assuming it's not null in the Excel file
        }

    }
    public static Stream<FilePojoConverter<Product>> getAllHelpers(){
        return Stream.of(csvPojoConverter, csvPojoConverter2);
    }
}