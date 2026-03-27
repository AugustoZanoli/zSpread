package br.com.clean.core.spreadsheet;

import br.com.clean.annotations.Spreadsheet;
import br.com.clean.exceptions.SpreadsheetMappingException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class Importer {

    /**
     * Importa planilha .xlsx mapeando cada linha para um objeto T.
     * Usa @Spreadsheet(name, ordem, notNull) nos campos da classe.
     *
     * Performance: reflexão e lookup feitos UMA VEZ antes do loop de linhas.
     */
    public <T> List<T> importSpreadsheet(byte[] spreadsheetBytes, Class<T> clazz) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(spreadsheetBytes))) {

            Sheet sheet = workbook.getSheetAt(0);

            // índice -> nome da coluna
            Map<Integer, String> headerMap = buildHeaderMap(sheet);

            // Reflexão feita uma única vez: nome da coluna -> Field
            Map<String, Field> columnToField = buildColumnToFieldMap(clazz);

            // índice da coluna -> Field (O(1) no loop)
            Map<Integer, Field> indexToField = buildIndexToFieldMap(headerMap, columnToField);

            //  Valida colunas obrigatórias existem no cabeçalho
            validateRequiredColumns(clazz, headerMap);

            // Itera linhas e popula objetos
            return buildObjects(sheet, clazz, indexToField);
        }
    }

    private Map<Integer, String> buildHeaderMap(Sheet sheet) {
        Row headerRow = sheet.getRow(0);

        if (headerRow == null) {
            throw new SpreadsheetMappingException("Planilha sem cabeçalho na linha 1.");
        }

        Map<Integer, String> headerMap = new HashMap<>();
        headerRow.forEach(cell ->
                headerMap.put(cell.getColumnIndex(), cell.getStringCellValue().trim())
        );
        return headerMap;
    }

    private <T> Map<String, Field> buildColumnToFieldMap(Class<T> clazz) {
        Map<String, Field> map = new HashMap<>();

        for (Field field : clazz.getDeclaredFields()) {
            Spreadsheet annotation = field.getAnnotation(Spreadsheet.class);
            if (annotation != null) {
                field.setAccessible(true);
                map.put(annotation.name().trim(), field);
            }
        }
        return map;
    }

    private Map<Integer, Field> buildIndexToFieldMap(
            Map<Integer, String> headerMap,
            Map<String, Field> columnToField) {

        Map<Integer, Field> indexToField = new HashMap<>();

        headerMap.forEach((index, columnName) -> {
            Field field = columnToField.get(columnName);
            if (field != null) {
                indexToField.put(index, field);
            }
        });
        return indexToField;
    }

    private <T> void validateRequiredColumns(Class<T> clazz, Map<Integer, String> headerMap) {
        Set<String> presentHeaders = new HashSet<>(headerMap.values());

        for (Field field : clazz.getDeclaredFields()) {
            Spreadsheet annotation = field.getAnnotation(Spreadsheet.class);
            if (annotation != null && annotation.notNull()) {
                if (!presentHeaders.contains(annotation.name().trim())) {
                    throw new SpreadsheetMappingException(
                            "Coluna obrigatória '%s' não encontrada no cabeçalho da planilha."
                                    .formatted(annotation.name())
                    );
                }
            }
        }
    }

    private <T> List<T> buildObjects(Sheet sheet, Class<T> clazz, Map<Integer, Field> indexToField) {
        List<T> results = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null || isRowEmpty(row)) continue;

            try {
                T instance = clazz.getDeclaredConstructor().newInstance();

                row.forEach(cell -> {
                    Field field = indexToField.get(cell.getColumnIndex());
                    if (field == null) return;

                    Spreadsheet annotation = field.getAnnotation(Spreadsheet.class);
                    Object value = extractCellValue(cell, field.getType(), formatter);

                    if (value == null && annotation.notNull()) {
                        throw new SpreadsheetMappingException(
                                "Campo obrigatório '%s' está vazio na linha %d, coluna %d."
                                        .formatted(field.getName(), cell.getRowIndex() + 1, cell.getColumnIndex() + 1)
                        );
                    }

                    if (value == null) return;

                    try {
                        field.set(instance, value);
                    } catch (IllegalAccessException e) {
                        throw new SpreadsheetMappingException(
                                "Erro ao setar campo '%s' na linha %d."
                                        .formatted(field.getName(), cell.getRowIndex() + 1), e
                        );
                    }
                });

                results.add(instance);

            } catch (ReflectiveOperationException e) {
                throw new SpreadsheetMappingException(
                        "Erro ao instanciar '%s' na linha %d.".formatted(clazz.getSimpleName(), i + 1), e
                );
            }
        }
        return results;
    }

    private Object extractCellValue(Cell cell, Class<?> targetType, DataFormatter formatter) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;

        return switch (cell.getCellType()) {

            case STRING -> {
                String raw = cell.getStringCellValue().trim();
                yield raw.isEmpty() ? null : convertString(raw, targetType);
            }

            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield convertDate(cell, targetType);
                }
                yield convertNumeric(cell.getNumericCellValue(), targetType);
            }

            case BOOLEAN -> {
                boolean val = cell.getBooleanCellValue();
                if (targetType == Boolean.class || targetType == boolean.class) yield val;
                yield String.valueOf(val);
            }

            case FORMULA -> {
                String evaluated = formatter.formatCellValue(cell);
                yield evaluated.isBlank() ? null : convertString(evaluated.trim(), targetType);
            }

            default -> null;
        };
    }

    private Object convertString(String value, Class<?> type) {
        if (type == String.class) return value;
        if (type == Integer.class || type == int.class) return Integer.parseInt(value);
        if (type == Long.class || type == long.class) return Long.parseLong(value);
        if (type == Double.class || type == double.class) return Double.parseDouble(value);
        if (type == BigDecimal.class) return new BigDecimal(value);
        if (type == Boolean.class || type == boolean.class) return Boolean.parseBoolean(value);
        return value;
    }

    private Object convertNumeric(double value, Class<?> type) {
        if (type == Integer.class
                || type == int.class) return (int) value;
        if (type == Long.class
                || type == long.class) return (long) value;
        if (type == Double.class
                || type == double.class) return value;
        if (type == BigDecimal.class) return BigDecimal.valueOf(value);
        if (type == String.class) return formatNumeric(value);
        return value;
    }

    private Object convertDate(Cell cell, Class<?> type) {
        if (type == LocalDate.class) return cell.getLocalDateTimeCellValue().toLocalDate();
        if (type == LocalDateTime.class) return cell.getLocalDateTimeCellValue();
        if (type == Date.class) return cell.getDateCellValue();
        return cell.getLocalDateTimeCellValue().toLocalDate();
    }

    private String formatNumeric(double value) {
        return value == Math.floor(value)
                ? String.valueOf((long) value)
                : String.valueOf(value);
    }

    private boolean isRowEmpty(Row row) {
        for (Cell cell : row) {
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}