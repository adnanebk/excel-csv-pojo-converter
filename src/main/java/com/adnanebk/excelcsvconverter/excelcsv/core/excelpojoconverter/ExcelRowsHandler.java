package com.adnanebk.excelcsvconverter.excelcsv.core.excelpojoconverter;

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


    public ExcelRowsHandler(ReflectionHelper<T> reflectionHelper) {
        this.reflectionHelper = reflectionHelper;
    }

    public void fillRowFromObject(Row row, T obj) {
        var fields = reflectionHelper.getFields();
        for (int i = 0; i < fields.size(); i++) {
            var field = fields.get(i);
            setCellValue(field.getTypeName(), row.createCell(i), field.getValue(obj));
        }
    }

    public T convertToObject(Row currentRow) {
        T obj = reflectionHelper.createInstance();

        for (var field : reflectionHelper.getFields()) {
            try {
                var cell = currentRow.getCell(field.getCellIndex(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                if(!cell.getCellType().equals(CellType.BLANK))
                  field.setValue(getCellValue(field, cell), obj);
            } catch (IllegalStateException | NumberFormatException e) {
                throw new SheetValidationException(String.format("Unexpected or Invalid cell value in row %s, column %s", currentRow.getRowNum() + 1, ALPHABET.charAt(field.getCellIndex())));
            } catch (DateTimeException e) {
                throw new SheetValidationException(String.format("Invalid or unsupported date pattern in row %s, column %s", currentRow.getRowNum() + 1, ALPHABET.charAt(field.getCellIndex())));
            }
        }
        return obj;
    }

    private Object getCellValue(ReflectedField<?> reflectedField,Cell cell) {
        var t = cell.getCellType();
        if (cell.getCellType().equals(CellType.STRING))
            return cell.getStringCellValue();
        return switch (reflectedField.getTypeName()) {
            case "number" -> dataFormat.formatCellValue(cell).replace(",", ".");
            case "localdate" -> cell.getLocalDateTimeCellValue().toLocalDate();
            case "localdatetime" -> cell.getLocalDateTimeCellValue();
            case "zoneddatetime" -> cell.getLocalDateTimeCellValue().atZone(ZoneId.systemDefault());
            case "date" -> cell.getDateCellValue();
            default -> throw new IllegalStateException();
        };
    }

    private void setCellValue(String fieldType, Cell cell, Object fieldValue) {
        if (fieldValue == null)
            return;
        if (fieldType.equals("number"))
            cell.setCellValue(Double.parseDouble(fieldValue.toString()));
        else
            cell.setCellValue(fieldValue.toString());
    }

}
