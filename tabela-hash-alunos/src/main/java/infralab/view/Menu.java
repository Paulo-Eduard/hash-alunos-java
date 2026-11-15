package infralab.view;

import infralab.model.Aluno;
import infralab.structures.TabelaHash;
import infralab.structures.TabelaHash.ResultadoInsercao;

import java.io.PrintStream;
import java.util.Scanner;

//Interface de console. Toda leitura é feita como texto e convertida com validação
public class Menu {

    private final Scanner entrada;
    private final PrintStream saida;
    private final TabelaHash tabela;

    public Menu(Scanner entrada, PrintStream saida, TabelaHash tabela) {
        this.entrada = entrada;
        this.saida = saida;
        this.tabela = tabela;
    }

    public void executar() {
        boolean rodando = true;
        while (rodando) {
            exibirMenu();
            String linha = lerLinha();
            if (linha == null) { // fim da entrada (Ctrl+D / Ctrl+Z)
                break;
            }
            switch (linha.trim()) {
                case "1": inserir(); break;
                case "2": buscar(); break;
                case "3": remover(); break;
                case "4": saida.println();
                          saida.print(tabela.exibir()); break;
                case "5": rodando = false; break;
                default:  saida.println("\n[ERRO] Opção inválida. Digite um número de 1 a 5.");
            }
        }
        saida.println("\nSistema encerrado. Até logo!");
    }

    private void exibirMenu() {
        saida.println();
        saida.println("========================================");
        saida.println("       SISTEMA DE TABELA HASH");
        saida.println("          GERENCIAMENTO DE ALUNOS");
        saida.println("========================================");
        saida.println();
        saida.println("1 - Inserir novo aluno");
        saida.println("2 - Buscar aluno");
        saida.println("3 - Remover aluno");
        saida.println("4 - Exibir Tabela Hash");
        saida.println("5 - Encerrar sistema");
        saida.println();
        saida.println("========================================");
        saida.println();
        saida.print("Escolha uma opção: ");
    }

    private void inserir() {
        Integer matricula = lerInteiro("Matrícula: ", 1, Integer.MAX_VALUE);
        if (matricula == null) return;

        String nome = lerNome();
        if (nome == null) return;

        Integer idade = lerInteiro("Idade: ", 1, 120);
        if (idade == null) return;

        int indice = tabela.hash(matricula);
        ResultadoInsercao r = tabela.inserir(new Aluno(matricula, nome, idade));
        saida.println();
        saida.println("h(" + matricula + ") = " + matricula + " % " + TabelaHash.TAMANHO + " = " + indice);
        switch (r) {
            case INSERIDO:
                saida.println("[OK] Aluno inserido no índice " + indice + " (posição estava vazia).");
                break;
            case INSERIDO_COM_COLISAO:
                saida.println("[OK] COLISÃO no índice " + indice + ": aluno inserido no FINAL da lista.");
                break;
            case DUPLICADO:
                saida.println("[ERRO] Matrícula " + matricula + " já cadastrada. Nada foi inserido.");
                break;
        }
    }

    private void buscar() {
        Integer matricula = lerInteiro("Matrícula a buscar: ", 1, Integer.MAX_VALUE);
        if (matricula == null) return;

        int indice = tabela.hash(matricula);
        saida.println();
        saida.println("h(" + matricula + ") = " + matricula + " % " + TabelaHash.TAMANHO + " = " + indice
                + "  (percorrendo somente a lista do índice " + indice + ")");
        Aluno a = tabela.buscar(matricula);
        if (a == null) {
            saida.println("[AVISO] Aluno com matrícula " + matricula + " não está cadastrado.");
        } else {
            saida.println("[ENCONTRADO]");
            saida.println("  Matrícula: " + a.getMatricula());
            saida.println("  Nome     : " + a.getNome());
            saida.println("  Idade    : " + a.getIdade());
        }
    }

    private void remover() {
        Integer matricula = lerInteiro("Matrícula a remover: ", 1, Integer.MAX_VALUE);
        if (matricula == null) return;

        int indice = tabela.hash(matricula);
        saida.println();
        saida.println("h(" + matricula + ") = " + matricula + " % " + TabelaHash.TAMANHO + " = " + indice);
        Aluno a = tabela.remover(matricula);
        if (a == null) {
            saida.println("[AVISO] Matrícula " + matricula + " não encontrada. Nada foi removido.");
        } else {
            saida.println("[OK] Aluno removido: " + a);
        }
    }

    // leitura com validação 

    private String lerLinha() {
        return entrada.hasNextLine() ? entrada.nextLine() : null;
    }

    //Lê um inteiro no intervalo [min, max], retorna null se a entrada for inválida ou acabar
    private Integer lerInteiro(String rotulo, int min, int max) {
        saida.print(rotulo);
        String linha = lerLinha();
        if (linha == null) return null;
        try {
            int valor = Integer.parseInt(linha.trim());
            if (valor < min || valor > max) {
                saida.println("[ERRO] Valor fora do intervalo permitido (" + min
                        + (max == Integer.MAX_VALUE ? " ou mais" : " a " + max) + "). Operação cancelada.");
                return null;
            }
            return valor;
        } catch (NumberFormatException e) {
            saida.println("[ERRO] Entrada inválida: digite um número inteiro. Operação cancelada.");
            return null;
        }
    }

    private String lerNome() {
        saida.print("Nome: ");
        String linha = lerLinha();
        if (linha == null) return null;
        String nome = linha.trim();
        if (nome.isEmpty()) {
            saida.println("[ERRO] O nome não pode ser vazio. Operação cancelada.");
            return null;
        }
        return nome;
    }
}
