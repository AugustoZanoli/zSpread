# zSpread

A Java library designed to simplify Excel and Word file export and import, eliminating boilerplate code through annotation-based field mapping and reflection.

---

## The Problem

Exporting and importing spreadsheets in Java usually means writing the same repetitive code over and over — manually mapping column names, building extractors field by field, and repeating that pattern for every entity in your project.

zSpread eliminates that by letting you describe your data with annotations and handling the rest automatically.

---

## How it will work

Annotate your DTO fields with `@Coluna`:

```java
public class ContatoDTO {

    @Coluna(nome = "Nome", ordem = 1)
    private String nome;

    @Coluna(nome = "Documento", ordem = 2)
    private String documento;

    @Coluna(nome = "Email", ordem = 3)
    private String email;
}
```

Then just call:

```java
// Export
byte[] planilha = zSpread.exportar(contatos);

// Import
ImportacaoResultado<ContatoDTO> resultado = zSpread.importar(arquivo, ContatoDTO.class);
List<ContatoDTO> contatos = resultado.getDados();
```

---

## Supported formats

| Format | Export | Import |
|--------|--------|--------|
| `.xlsx` | 🔜 | 🔜 |
| `.docx` | 🔜 | 🔜 |

---

## Requirements

- Java 17+
- No Spring required — works with any Java project

---

## Installation

> Coming soon — will be available on Maven Central.

---

## License

> Coming soon.
