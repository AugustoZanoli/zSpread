package spreadhseetTests;

import br.com.clean.ZSpread;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class TesteExport {

    public static void main(String[] args) throws IOException {
        TesteDTO testeDTO1 = new TesteDTO("João", "Silva", "Engenheiro");
        TesteDTO testeDTO2 = new TesteDTO("Maria", "Santos", "Médica");
        TesteDTO testeDTO3 = new TesteDTO("Pedro", "Oliveira", "Professor");

        byte[] bytes = ZSpread.exportSpreadsheet(List.of(testeDTO1, testeDTO2, testeDTO3), TesteDTO.class, "Teste");

        System.out.println(bytes.length);

        new FileOutputStream("teste.xlsx").write(bytes);
    }
}
