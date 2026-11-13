package infralab;

import infralab.model.Aluno;
import infralab.structures.TabelaHash;
import infralab.structures.TabelaHash.ResultadoInsercao;
import infralab.view.Menu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

/** Testes sem bibliotecas externas. Execução: java -cp out infralab.TabelaHashTest */
public class TabelaHashTest {

    private static int ok = 0, falhas = 0;

    private static void check(String nome, boolean condicao) {
        if (condicao) { ok++; System.out.println("  [PASSOU] " + nome); }
        else          { falhas++; System.out.println("  [FALHOU] " + nome); }
    }

    private static Aluno a(int mat, String nome, int idade) { return new Aluno(mat, nome, idade); }

    /** 1001, 1012 e 1023 têm o mesmo resto na divisão por 11 (0): colidem no índice 0. */
    private static TabelaHash tabelaComTres() {
        TabelaHash t = new TabelaHash();
        t.inserir(a(1001, "João", 20));
        t.inserir(a(1012, "Maria", 22));
        t.inserir(a(1023, "Pedro", 19));
        return t;
    }

    public static void main(String[] args) {
        System.out.println("=== TESTES DA TABELA HASH ===");

        System.out.println("\n0. Função Hash e matrículas de exemplo");
        TabelaHash h = new TabelaHash();
        check("h(1001) == h(1012) == h(1023)", h.hash(1001) == h.hash(1012) && h.hash(1012) == h.hash(1023));
        check("h(1001) == 1001 % 11 == 0", h.hash(1001) == 0);
        check("h(1002) == 1", h.hash(1002) == 1);
        check("tamanho da tabela é 11", TabelaHash.TAMANHO == 11);

        System.out.println("\n1. Cadastro de um aluno");
        TabelaHash t1 = new TabelaHash();
        check("retorna INSERIDO", t1.inserir(a(1001, "João", 20)) == ResultadoInsercao.INSERIDO);
        check("quantidade == 1", t1.getQuantidade() == 1);

        System.out.println("\n2. Cadastro de vários alunos (índices distintos)");
        TabelaHash t2 = new TabelaHash();
        for (int m = 1; m <= 11; m++) t2.inserir(a(m, "Aluno" + m, 18 + m));
        check("11 alunos cadastrados", t2.getQuantidade() == 11);
        boolean todosSemColisao = true;
        for (int i = 0; i < 11; i++) if (t2.matriculasNaPosicao(i).length != 1) todosSemColisao = false;
        check("cada posição possui exatamente 1 aluno", todosSemColisao);

        System.out.println("\n3. Inserção com colisão");
        TabelaHash t3 = new TabelaHash();
        t3.inserir(a(1001, "João", 20));
        check("segundo aluno retorna INSERIDO_COM_COLISAO",
                t3.inserir(a(1012, "Maria", 22)) == ResultadoInsercao.INSERIDO_COM_COLISAO);
        check("ambos na mesma posição", t3.matriculasNaPosicao(t3.hash(1001)).length == 2);

        System.out.println("\n4. Inserção no FINAL da lista");
        TabelaHash t4 = tabelaComTres();
        int[] ordem = t4.matriculasNaPosicao(t4.hash(1001));
        check("ordem da lista == [1001, 1012, 1023] (não invertida)",
                Arrays.equals(ordem, new int[]{1001, 1012, 1023}));

        System.out.println("\n5. Matrícula duplicada");
        TabelaHash t5 = tabelaComTres();
        check("duplicada na cabeça -> DUPLICADO", t5.inserir(a(1001, "X", 30)) == ResultadoInsercao.DUPLICADO);
        check("duplicada no último nó -> DUPLICADO", t5.inserir(a(1023, "Y", 30)) == ResultadoInsercao.DUPLICADO);
        check("quantidade continua 3", t5.getQuantidade() == 3);
        check("dados originais preservados", t5.buscar(1023).getNome().equals("Pedro"));

        System.out.println("\n6. Busca de aluno existente");
        TabelaHash t6 = tabelaComTres();
        Aluno enc = t6.buscar(1012);
        check("encontra 1012", enc != null);
        check("dados completos corretos", enc != null && enc.getNome().equals("Maria") && enc.getIdade() == 22);

        System.out.println("\n7. Busca de aluno inexistente");
        check("matrícula 999 retorna null", t6.buscar(999) == null);
        check("matrícula de mesma posição mas ausente (1034) retorna null", t6.buscar(1034) == null);

        System.out.println("\n8. Remoção do primeiro nó");
        TabelaHash t8 = tabelaComTres();
        check("remove 1001", t8.remover(1001) != null);
        check("lista passa a ser [1012, 1023]",
                Arrays.equals(t8.matriculasNaPosicao(t8.hash(1001)), new int[]{1012, 1023}));

        System.out.println("\n9. Remoção de nó intermediário");
        TabelaHash t9 = tabelaComTres();
        check("remove 1012", t9.remover(1012) != null);
        check("lista passa a ser [1001, 1023]",
                Arrays.equals(t9.matriculasNaPosicao(t9.hash(1001)), new int[]{1001, 1023}));
        check("1023 continua acessível", t9.buscar(1023) != null);

        System.out.println("\n10. Remoção do último nó");
        TabelaHash t10 = tabelaComTres();
        check("remove 1023", t10.remover(1023) != null);
        check("lista passa a ser [1001, 1012]",
                Arrays.equals(t10.matriculasNaPosicao(t10.hash(1001)), new int[]{1001, 1012}));
        check("novo inserido vai ao final",
                t10.inserir(a(1034, "Ana", 21)) == ResultadoInsercao.INSERIDO_COM_COLISAO
                && Arrays.equals(t10.matriculasNaPosicao(t10.hash(1001)), new int[]{1001, 1012, 1034}));

        System.out.println("\n10b. Remoção do único nó");
        TabelaHash t10b = new TabelaHash();
        t10b.inserir(a(5, "Solo", 20));
        check("remove e posição fica vazia",
                t10b.remover(5) != null && t10b.matriculasNaPosicao(5).length == 0 && t10b.getQuantidade() == 0);

        System.out.println("\n11. Remoção de matrícula inexistente");
        TabelaHash t11 = tabelaComTres();
        check("retorna null", t11.remover(9999) == null);
        check("quantidade continua 3", t11.getQuantidade() == 3);
        check("tabela vazia: remover retorna null", new TabelaHash().remover(1) == null);

        System.out.println("\n12. Exibição das 11 posições");
        TabelaHash t12 = tabelaComTres();
        t12.inserir(a(1002, "Lucas", 19));
        String texto = t12.exibir();
        check("contém 11 linhas", texto.split("\\R").length == 11);
        check("contém ÍNDICE 0 e ÍNDICE 10", texto.contains("ÍNDICE  0") && texto.contains("ÍNDICE 10"));
        check("mostra 'Vazio' para posições sem alunos", texto.contains("Vazio"));
        check("marca colisão com 3 alunos", texto.contains("COLISÃO (3 alunos)"));
        check("mostra encadeamento com setas",
                texto.contains("[1001 | João | 20] → [1012 | Maria | 22] → [1023 | Pedro | 19]"));
        System.out.println(texto);

        System.out.println("13. Tratamento de entradas inválidas (Menu simulado)");
        String entrada = String.join("\n",
                "abc",                 // opção inválida (texto)
                "9",                   // opção fora do intervalo
                "",                    // opção vazia
                "1", "xyz",            // matrícula não numérica
                "1", "-5",             // matrícula negativa
                "1", "0",              // matrícula zero
                "1", "1001", "   ",    // nome vazio
                "1", "1001", "João", "abc",  // idade não numérica
                "1", "1001", "João", "200",  // idade fora do intervalo
                "1", "99999999999",    // matrícula acima de int
                "2", "texto",          // busca inválida
                "3", "-1",             // remoção inválida
                "4",                   // exibe (tabela deve estar vazia)
                "5") + "\n";
        TabelaHash tm = new TabelaHash();
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(buf, true, StandardCharsets.UTF_8);
        Scanner sc = new Scanner(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        boolean semExcecao = true;
        try { new Menu(sc, ps, tm).executar(); } catch (Exception e) { semExcecao = false; e.printStackTrace(); }
        String saida = buf.toString(StandardCharsets.UTF_8);
        check("programa não encerrou com exceção", semExcecao);
        check("opção inválida tratada", saida.contains("Opção inválida"));
        check("entrada não numérica tratada", saida.contains("digite um número inteiro"));
        check("valor fora do intervalo tratado", saida.contains("fora do intervalo"));
        check("nome vazio tratado", saida.contains("nome não pode ser vazio"));
        check("nenhum aluno foi cadastrado com dados inválidos", tm.getQuantidade() == 0);
        check("mensagem de encerramento exibida", saida.contains("Sistema encerrado"));

        System.out.println("\n14. Fluxo completo pelo Menu (inserir, colisão, duplicada, buscar, remover)");
        String fluxo = String.join("\n",
                "1","1001","João","20",
                "1","1012","Maria","22",
                "1","1001","Outro","30",
                "2","1012",
                "3","1001",
                "2","1001",
                "5") + "\n";
        TabelaHash tf = new TabelaHash();
        ByteArrayOutputStream buf2 = new ByteArrayOutputStream();
        new Menu(new Scanner(new ByteArrayInputStream(fluxo.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8),
                new PrintStream(buf2, true, StandardCharsets.UTF_8), tf).executar();
        String s2 = buf2.toString(StandardCharsets.UTF_8);
        check("mensagem de colisão exibida", s2.contains("COLISÃO no índice"));
        check("mensagem de duplicidade exibida", s2.contains("já cadastrada"));
        check("busca encontrou Maria", s2.contains("Maria") && s2.contains("[ENCONTRADO]"));
        check("remoção confirmada", s2.contains("Aluno removido"));
        check("busca após remoção não encontra", s2.contains("não está cadastrado"));
        check("estado final: só 1012 permanece",
                tf.getQuantidade() == 1 && tf.buscar(1012) != null && tf.buscar(1001) == null);

        System.out.println("\n=== RESULTADO: " + ok + " passaram, " + falhas + " falharam ===");
        if (falhas > 0) System.exit(1);
    }
}
