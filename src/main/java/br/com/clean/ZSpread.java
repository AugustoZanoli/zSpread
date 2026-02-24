package br.com.clean;


import br.com.clean.core.spreadsheet.Spreadsheet;
import br.com.clean.validator.Validator;

import java.util.List;

public class ZSpread {

    public static <T> byte[] exportSpreadsheet(List<T> object, Class<T> clazz, String spreadsheetName) {
        Validator validator = new Validator();
        Spreadsheet spreadsheet = new Spreadsheet(validator);

        return spreadsheet.export(object, clazz, spreadsheetName);
    }
}
