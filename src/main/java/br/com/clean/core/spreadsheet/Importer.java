package br.com.clean.core.spreadsheet;

import br.com.clean.annotations.Spreadsheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Importer {

    Map<Object, Field> annotatedFields = Map.of();

    public <T> List<T> importSpreadsheet(byte[] spreadsheetBytes, Class<T> clazz) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(spreadsheetBytes);

        Field[] objectFields = clazz.getDeclaredFields();

        Workbook workbook = new XSSFWorkbook(inputStream);

        getCells(workbook, objectFields);


        return List.of();
    }

    private void getCells(Workbook workbook, Field[] objectFields) {
        Sheet sheet = workbook.getSheetAt(0);

        Map<Integer, String> headerMap = getHeader(sheet);

        getBody(sheet, objectFields, headerMap);

    }

    private Map<Integer, String> getHeader(Sheet sheet) {
        Map<Integer, String> headerMap = new HashMap<>();

        Row header = sheet.getRow(0);
        header.forEach(cell -> {
            headerMap.put(cell.getColumnIndex(), cell.getStringCellValue());
        });

        return headerMap;
    }

    private void getBody(Sheet sheet, Field[] objectFields, Map<Integer, String> headerMap) {
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            row.forEach(cell -> {
                int columnIndex = cell.getColumnIndex();
                String headerValue = cell.getStringCellValue();

                Field field = annotatedFields.get(headerValue);

                if (field != null) {
                    headerMap.entrySet().stream();
//                            .filter();
                }
            });
        }
    }
}
