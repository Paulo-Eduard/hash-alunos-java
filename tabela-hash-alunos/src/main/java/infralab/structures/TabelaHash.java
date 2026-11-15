package infralab.structures;

import infralab.model.Aluno;

/**
 * Tabela Hash com encadeamento (Separate Chaining), implementada manualmente.
 *
 *  - 11 posições (0 a 10)
 *  - Função Hash: h(chave) = chave % 11   (chave = matrícula)
 *  - Colisões: lista simplesmente encadeada; novo aluno entra SEMPRE no FIM.
 *  - Matrículas únicas em toda a tabela.
 */
public class TabelaHash {

    public static final int TAMANHO = 11;

    //Resultado possível de uma inserção.
    public enum ResultadoInsercao {
        INSERIDO,               // posição estava vazia
        INSERIDO_COM_COLISAO,   // posição ocupada; aluno entrou no final da lista
        DUPLICADO               // matrícula já cadastrada; nada foi inserido
    }

    // Cada posição guarda a referência para o PRIMEIRO nó da lista (ou null).
    private final No[] tabela = new No[TAMANHO];
    private int quantidade = 0;

    //Função Hash: resto da divisão da matrícula por 11.
    public int hash(int chave) {
        if (chave < 0) {
            throw new IllegalArgumentException("A matrícula não pode ser negativa.");
        }
        return chave % TAMANHO;
    }

    /**
     * Insere o aluno. Como a mesma matrícula sempre cai na mesma posição,
     * verificar duplicidade apenas na lista daquele índice garante unicidade global.
     */
    public ResultadoInsercao inserir(Aluno aluno) {
        int matricula = aluno.getMatricula();
        int indice = hash(matricula);
        No novo = new No(aluno);

        // Posição vazia: o novo nó vira o início da lista.
        if (tabela[indice] == null) {
            tabela[indice] = novo;
            quantidade++;
            return ResultadoInsercao.INSERIDO;
        }

        // Colisão: percorre até o ÚLTIMO nó, verificando duplicidade em cada um.
        No atual = tabela[indice];
        while (true) {
            if (atual.getAluno().getMatricula() == matricula) {
                return ResultadoInsercao.DUPLICADO;
            }
            if (atual.getProximo() == null) {
                break; // atual é o último nó
            }
            atual = atual.getProximo();
        }

        // Inserção no FINAL da lista (nunca no início).
        atual.setProximo(novo);
        quantidade++;
        return ResultadoInsercao.INSERIDO_COM_COLISAO;
    }

    //Busca acessando somente a lista do índice calculado. Retorna null se não existir.
    public Aluno buscar(int matricula) {
        No atual = tabela[hash(matricula)];
        while (atual != null) {
            if (atual.getAluno().getMatricula() == matricula) {
                return atual.getAluno();
            }
            atual = atual.getProximo();
        }
        return null;
    }

    /**
     * Remove o aluno e religa os nós. Retorna o aluno removido ou null se não existir.
     * Trata: primeiro nó, nó intermediário, último nó e matrícula inexistente.
     */
    public Aluno remover(int matricula) {
        int indice = hash(matricula);
        No anterior = null;
        No atual = tabela[indice];

        while (atual != null) {
            if (atual.getAluno().getMatricula() == matricula) {
                if (anterior == null) {
                    // Primeiro nó: a cabeça da lista passa a ser o próximo.
                    tabela[indice] = atual.getProximo();
                } else {
                    // Intermediário ou último: o anterior "pula" o nó removido.
                    anterior.setProximo(atual.getProximo());
                }
                quantidade--;
                return atual.getAluno();
            }
            anterior = atual;
            atual = atual.getProximo();
        }
        return null; // matrícula inexistente
    }

    public int getQuantidade() { return quantidade; }

    //Matrículas de uma posição, na ordem da lista (útil para testes).
    public int[] matriculasNaPosicao(int indice) {
        if (indice < 0 || indice >= TAMANHO) {
            throw new IllegalArgumentException("Índice fora do intervalo 0-" + (TAMANHO - 1));
        }
        int n = 0;
        for (No p = tabela[indice]; p != null; p = p.getProximo()) n++;
        int[] resultado = new int[n];
        int i = 0;
        for (No p = tabela[indice]; p != null; p = p.getProximo()) {
            resultado[i++] = p.getAluno().getMatricula();
        }
        return resultado;
    }

    // Alunos de uma posição, na ordem da lista (usado pela interface web)
    public Aluno[] alunosNaPosicao(int indice) {
        if (indice < 0 || indice >= TAMANHO) {
            throw new IllegalArgumentException("Índice fora do intervalo 0-" + (TAMANHO - 1));
        }
        int n = 0;
        for (No p = tabela[indice]; p != null; p = p.getProximo()) n++;
        Aluno[] resultado = new Aluno[n];
        int i = 0;
        for (No p = tabela[indice]; p != null; p = p.getProximo()) {
            resultado[i++] = p.getAluno();
        }
        return resultado;
    }

    //Monta a visualização das 11 posições, destacando colisões
    public String exibir() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TAMANHO; i++) {
            sb.append(String.format("ÍNDICE %2d → ", i));
            No atual = tabela[i];
            if (atual == null) {
                sb.append("Vazio");
            } else {
                int tamanhoLista = 0;
                while (atual != null) {
                    if (tamanhoLista > 0) sb.append(" → ");
                    sb.append(atual.getAluno());
                    tamanhoLista++;
                    atual = atual.getProximo();
                }
                if (tamanhoLista > 1) {
                    sb.append("   <== COLISÃO (").append(tamanhoLista).append(" alunos)");
                }
            }
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }
}
