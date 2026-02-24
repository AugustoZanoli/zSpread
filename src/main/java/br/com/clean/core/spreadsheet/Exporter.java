package br.com.clean.core.spreadsheet;

import br.com.clean.annotations.Spreadsheet;
import br.com.clean.exceptions.GenerateBodyException;
import br.com.clean.exceptions.GenerateBytesExportException;
import br.com.clean.exceptions.GenerateWorkbookException;
import br.com.clean.validator.Validator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Exporter {

    private final Validator validator;

    public Exporter(Validator validator) {
        this.validator = validator;
    }

    public <T> byte[] export(List<T> objects, Class<T> clazz, String spreadsheetName) {

        validator.validateObjects(objects);

        validator.validateString(spreadsheetName);

        Field[] objectFields = clazz.getDeclaredFields();

        List<Field> annotatedFields = Stream.of(objectFields)
                .filter(field -> field.isAnnotationPresent(Spreadsheet.class))
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(Spreadsheet.class).ordem()))
                .toList();

        validator.validateObjects(annotatedFields);

        Workbook workbook = generateWorkbook(objects, annotatedFields, spreadsheetName);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new GenerateBytesExportException("Failed generating bytes from the generated spreadsheet!", e);
        }
    }

    private <T> Workbook generateWorkbook(List<T> objects, List<Field> annotatedFields, String spreadsheetName) {
        try {
            // Creating the workbook and sheet
            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(spreadsheetName);

            generateHeader(annotatedFields, sheet);

            generateBody(objects, annotatedFields, sheet);

            for (int i = 0; i < annotatedFields.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            return workbook;
        } catch (Exception e) {
            throw new GenerateWorkbookException("Failed to generate the workbook!", e);
        }
    }

    private static void generateHeader(List<Field> annotatedFields, Sheet sheet) {

        CellStyle style = createHeaderStyle(sheet);

        Row headerRow = sheet.createRow(0);

        // Creating the header row based on the annotated fields
        for (Field field : annotatedFields){
            Spreadsheet colunaAnnotation = field.getAnnotation(Spreadsheet.class);
            String columnName = colunaAnnotation.name();
            int columnIndex = annotatedFields.indexOf(field);

            Cell cell = headerRow.createCell(columnIndex);
            cell.setCellValue(columnName);
            cell.setCellStyle(style);
        }
    }

    private static CellStyle createHeaderStyle(Sheet sheet) {
        CellStyle style = sheet.getWorkbook().createCellStyle();
        style.setFillForegroundColor(IndexedColors.AQUA.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.LEFT);
        return style;
    }

    private <T> void generateBody(List<T> objects, List<Field> annotatedFields, Sheet sheet) {

        // Creating the body rows based on the annotated fields and the list of objects
        for (int i = 0; i < objects.size(); i++) {
            Object object = objects.get(i);

            Row row = sheet.getRow(i + 1);
            if (row == null) {
                row = sheet.createRow(i + 1);
            }

            for (Field field : annotatedFields) {
                int columnIndex = annotatedFields.indexOf(field);
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    Cell cell = row.createCell(columnIndex);
                    cell.setCellValue(value != null ? value.toString() : "");
                } catch (IllegalAccessException e) {
                    throw new GenerateBodyException("Failed to read field '" + field.getName() + "' while generating body row " + i, e);                }
            }
        }
    }
}

