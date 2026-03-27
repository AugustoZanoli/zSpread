package br.com.clean.exceptions;

public class GenerateBodyException extends RuntimeException {
    public GenerateBodyException(String message, Throwable err) {
        super(message, err);
    }
}
