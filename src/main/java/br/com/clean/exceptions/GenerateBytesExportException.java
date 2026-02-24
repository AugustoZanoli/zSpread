package br.com.clean.exceptions;

public class GenerateBytesExportException extends RuntimeException {
    public GenerateBytesExportException(String message, Throwable err) {
        super(message, err);
    }
}
