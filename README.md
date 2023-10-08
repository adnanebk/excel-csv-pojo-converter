[![Quality gate](https://sonarcloud.io/api/project_badges/quality_gate?project=adnanebk_excel-pojo-converter)](https://sonarcloud.io/summary/new_code?id=adnanebk_excel-pojo-converter)

Converting Excel or CSV files into Java objects (POJOs) and vice versa can be a complex process, but with the right tools and techniques, it becomes much more manageable. In this guide, we’ll explore a powerful Java library that leverages Java reflection.

## First we add the dependency to maven

```
<dependency>
  <groupId>com.adnanebk</groupId>
  <artifactId>excel-csv-converter</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```
## Understanding the POJO Class

Before we dive into the library, let’s take a close look at a sample Java class that serves as our data model:

```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SheetDefinition(datePattern = "dd/MM/yyyy")
public class Product {

    @CellDefinition(0)
    private String name;

    @CellDefinition(1)
    private long price;

    @CellDefinition(2)
    @CellBoolean(trueValue = "yes",falseValue = "no")
    private boolean active;

    @CellDefinition(value = 3, title = "Promo price")
    private double promoPrice;

    // Additional fields...

    @CellEnum(enumsMapperMethod = "categoryMap")
    @CellDefinition(10)
    private Category category;

    @CellDefinition(11)
    private LocalDateTime localDateTime;

    private Map<Category,String> categoryMap(){
        return Map.of(Category.A,"Formatted A",
                      Category.B,"Formatted B");
    }
}
```
This Product class is annotated with various annotations that play a crucial role in the conversion process. Each field is annotated with @CellDefinition, indicating its position in the Excel or CSV file.

we can also define the title of the of the cell, by default it will convert the camel case name of the field to a name with spaces (ex: firstName=>First name)

The @SheetDefinition annotation provides additional information like date formatting patterns that will be used during conversion of date field types.

### The Enum Annotation: @CellEnum(enumsMapperMethod = "categoryMap")

In the Product class, we make use of the @CellEnum annotation in the enum Category field. the enumsMapperMethod argument allows us to define a method name, this method should return a map that define the mapping (conversions) between the enum constants and the formatted values in the excel/csv cells (by default the enum constants will be used)
note that the method name must much the enumsMapperMethod argument value.

### The Boolean Annotation: @CellBoolean(trueValue = "yes",falseValue = "no")

we make use of the @CellBoolean annotation in the boolean active field,it has two arguments witch represents the formatting values we want to use in the excel/csv fields.

Now, let’s introduce an updated version of our POJO class, ProductV2:
```
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SheetDefinition(includeAllFields = true,titles={"Name","Category","Date"})
public class ProductV2 {

    private String name;

    // Additional fields...

    private Category category;

    @IgnoreCell
    private LocalDateTime localDateTime;
}
```

when includeAllFields argument set to true the fields are automatically included and mapped in the cells based on its declared order and ignoring fields that annotated with @IgnoreCell annotation,

we can define the titles in the titles argument with the condition that they must be in the same order as the fields.

## Converting Excel/csv to POJOs and vice versa
```
@RestController
@RequestMapping("excel/products")
public class ExcelFieldsController {
    private final ExcelHelper<Product> excelHelper = ExcelHelper.create(Product.class);

    @GetMapping
    public List<Product> excelToProducts(@RequestBody MultipartFile file){
        return excelHelper.toStream(file.getInputStream()).toList();
    }

       @GetMapping("/download")
    public ResponseEntity<InputStreamResource>
    downloadExcelFromProducts() {
        String filename = "products-" + LocalDate.now() + ".xlsx";
        InputStreamResource file = new InputStreamResource(excelHelper.toExcel(getProducts()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }
}
```
the same applicable for converting csv files except we need to define the delimiter that will be used

    private final CsvHelper<ProductV2> csvHelper = CsvHelper.create(ProductV2.class,";");
## The ReflectionUtil Class: Dynamic Class Examination

The ReflectionUtil class serves as the backbone of this Java library, facilitating dynamic class examination and manipulation through the power of Java reflection.
One noteworthy feature of the ReflectionUtil class is the optimization applied to enhance performance. During initialization, all getters, setters, and fields are eagerly loaded and encapsulated in the SheetField Record. This deliberate action minimizes the need for reflection lookups in subsequent operations and boosts overall efficiency.

## SheetField Record Overview
```public record SheetField<T>(String typeName, String title, Function<T,Object> getter, BiConsumer<T,Object> setter, int colIndex)```

The Field record is a fundamental component of the library, designed to encapsulate information about the field of a class.

### Key Methods:
```public Object getValue(T obj)```: Retrieves the value of the field from an object using its getter method.

```public void setValue(T obj, Object value)```: Sets the value of the field in an object using its setter method.

## Conclusion :
By leveraging this custom library, developers can significantly simplify the process of converting Excel and CSV files to POJOs in Java. The integration of Java reflection, along with thoughtful design considerations, empowers dynamic mapping, making it a valuable tool for data processing tasks.
