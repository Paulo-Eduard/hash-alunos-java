package infralab.view;

import infralab.api.Servidor;
import infralab.structures.TabelaHash;

import java.nio.file.Path;

/** Ponto de entrada da versão web: serve a pasta web/ e a API em http://localhost:8080. */
public class MainWeb {
    public static void main(String[] args) throws Exception {
        int porta = 8080;
        new Servidor(new TabelaHash(), Path.of("web"), porta).iniciar();
        System.out.println("Servidor no ar: http://localhost:" + porta);
        System.out.println("Pressione Ctrl+C para encerrar.");
    }
}
