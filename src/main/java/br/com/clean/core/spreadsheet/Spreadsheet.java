package br.com.clean.core.spreadsheet;

import br.com.clean.annotations.Coluna;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class Spreadsheet {

    public <T> byte[] export(List<Object> objects, String spreadsheetName) {


        Class<T> objectClass = (Class<T>) objects.get(0).getClass();

        Field[] objectFields = objectClass.getDeclaredFields();

        List<Field> annotatedFields = Stream.of(objectFields)
                .filter(field -> field.isAnnotationPresent(br.com.clean.annotations.Coluna.class))
                .toList();

        Workbook workbook = generateWorkbook(objects, annotatedFields, spreadsheetName);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Workbook generateWorkbook(List<Object> objects, List<Field> annotatedFields, String spreadsheetName) {
        try {
            // Creating the workbook and sheet
            Workbook workbook = new XSSFWorkbook();

            Sheet sheet = workbook.createSheet(spreadsheetName);

            generateHeader(annotatedFields, sheet);

            generateBody(objects, annotatedFields, sheet);

            return workbook;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void generateHeader(List<Field> annotatedFields, Sheet sheet) {

        // Creating the header row based on the annotated fields
        for (Field field : annotatedFields){
            Coluna colunaAnnotation = field.getAnnotation(Coluna.class);
            String columnName = colunaAnnotation.name();
            int columnIndex = annotatedFields.indexOf(field);

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                headerRow = sheet.createRow(0);
            }

            Cell cell = headerRow.createCell(columnIndex);
            cell.setCellValue(columnName);
        }
    }

    private void generateBody(List<Object> objects, List<Field> annotatedFields, Sheet sheet) {

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
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

