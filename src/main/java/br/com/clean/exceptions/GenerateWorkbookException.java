package br.com.clean.exceptions;

public class GenerateWorkbookException extends RuntimeException {
    public GenerateWorkbookException(String message, Throwable err) {
        super(message, err);
    }
}
