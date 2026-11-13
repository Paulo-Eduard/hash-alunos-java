package infralab;

import infralab.structures.TabelaHash;
import infralab.view.Menu;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/** Ponto de entrada: cria a tabela e inicia o menu. */
public class Main {
    public static void main(String[] args) {
        PrintStream saida = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        Scanner entrada = new Scanner(System.in, StandardCharsets.UTF_8);
        new Menu(entrada, saida, new TabelaHash()).executar();
        entrada.close();
    }
}
