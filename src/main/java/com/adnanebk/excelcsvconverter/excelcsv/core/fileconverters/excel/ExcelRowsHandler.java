package com.adnanebk.excelcsvconverter.excelcsv.core.fileconverters.excel;

import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectedField;
import com.adnanebk.excelcsvconverter.excelcsv.core.reflection.ReflectionHelper;
import com.adnanebk.excelcsvconverter.excelcsv.exceptions.SheetValidationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.time.DateTimeException;
import java.time.ZoneId;

public class ExcelRowsHandler<T> {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private final ReflectionHelper<T> reflectionHelper;
    private final DataFormatter dataFormat = new DataFormatter();
    private final String[] headers;


    public ExcelRowsHandler(ReflectionHelper<T> reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
        this.headers = reflectionHelper.getFields().stream().map(ReflectedField::getTitle).toArray(String[]::new);
    }

    public String[] getHeaders(){
        return headers;
    }

    public void fillRowFromObject(Row row, T obj) {
        var fields = reflectionHelper.getFields();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            String fieldValue = field.getValueAsString(obj);
            if (fieldValue == null)
                return;
            var cell = row.createCell(i);
            if (field.getTypeName().equals("number"))
                cell.setCellValue(Double.parseDouble(fieldValue));
            else
                cell.setCellValue(fieldValue);
        }
    }

    public T convertToObject(Row row) {
        T obj = reflectionHelper.createInstance();
        for (var field : reflectionHelper.getFields()) {
            try {
                var cell = row.getCell(field.getCellIndex(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if(!cell.getCellType().equals(CellType.BLANK))
                 setFieldValue(field, cell, obj);
            } catch (IllegalStateException | NumberFormatException e) {
                throw new SheetValidationException(String.format("Cannot convert the cell value in row %s, column %s to the field %s", row.getRowNum() + 1, ALPHABET.charAt(field.getCellIndex()),field.getName()));
            } catch (DateTimeException e) {
                throw new SheetValidationException(String.format("Invalid or unsupported date pattern in row %s, column %s for the field %s", row.getRowNum() + 1, ALPHABET.charAt(field.getCellIndex()),field.getName()));
            }
        }
        return obj;
    }

    private void setFieldValue(ReflectedField<?> field, Cell cell, T obj) {
        if (cell.getCellType().equals(CellType.STRING))
             field.setValue(cell.getStringCellValue(), obj);
        else {
            switch (field.getTypeName()) {
                case "number" -> field.setValue(dataFormat.formatCellValue(cell), obj);
                case "localdate" -> field.setValue(cell.getLocalDateTimeCellValue().toLocalDate(),obj);
                case "localdatetime" -> field.setValue(cell.getLocalDateTimeCellValue(),obj);
                case "zoneddatetime" -> field.setValue(cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault()),obj);
                case "date" -> field.setValue(cell.getDateCellValue(),obj);
                default -> throw new IllegalStateException();
            }
        }
    }

}
