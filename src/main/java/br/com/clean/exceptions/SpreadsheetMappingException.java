package br.com.clean.exceptions;

public class SpreadsheetMappingException extends RuntimeException {

    public SpreadsheetMappingException(String message) {
        super(message);
    }

    public SpreadsheetMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}