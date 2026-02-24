package spreadhseetTests;

import br.com.clean.annotations.Coluna;

public class TesteDTO {

    @Coluna(name = "Nome")
    private String nome;

    @Coluna(name = "Sobrenome")
    private String sobrenome;

    private String trabalho;

    public TesteDTO(String nome, String sobrenome, String trabalho) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.trabalho = trabalho;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getSobrenome() {
        return sobrenome;
    }

    public void setSobrenome(String sobrenome) {
        this.sobrenome = sobrenome;
    }

    public String getTrabalho() {
        return trabalho;
    }

    public void setTrabalho(String trabalho) {
        this.trabalho = trabalho;
    }
}
