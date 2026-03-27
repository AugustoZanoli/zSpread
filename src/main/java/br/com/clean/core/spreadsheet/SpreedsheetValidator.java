package br.com.clean.core.spreadsheet;

import java.util.List;
import java.util.Objects;

public class SpreedsheetValidator {
    public <T> void validateObjects(List<T> objects) {
        if (Objects.isNull(objects)) throw new IllegalArgumentException("A lista de objetos não pode ser nula.");
        if (objects.isEmpty()) throw new IllegalArgumentException("A lista de objetos não pode estar vazia.");
    }

    public void validateString(String string) {
        if (Objects.isNull(string)) throw new IllegalArgumentException("A string não pode ser nula.");
        if (string.isBlank()) throw new IllegalArgumentException("A string não pode estar em branco.");
    }

}
