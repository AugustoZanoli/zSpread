package br.com.clean.core.spreadsheet;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Importer {

    public <T> List<T> importSpreadsheet(byte[] spreadsheetBytes, Class<T> clazz) throws IOException {

        InputStream inputStream = new ByteArrayInputStream(spreadsheetBytes);

        Workbook workbook = new XSSFWorkbook(inputStream);

        return List.of();
    }
}
