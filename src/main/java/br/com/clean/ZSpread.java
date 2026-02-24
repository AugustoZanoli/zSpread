package br.com.clean;


import br.com.clean.core.spreadsheet.Exporter;
import br.com.clean.validator.Validator;

import java.util.List;

public class ZSpread {

    public static <T> byte[] exportSpreadsheet(List<T> object, Class<T> clazz, String spreadsheetName) {
        Validator validator = new Validator();
        Exporter exporter = new Exporter(validator);

        return exporter.export(object, clazz, spreadsheetName);
    }
}
