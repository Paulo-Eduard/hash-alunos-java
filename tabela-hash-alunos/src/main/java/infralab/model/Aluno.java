package infralab.model;

/**
 * Representa o registro de um aluno.
 * A matrícula é a CHAVE usada pela função Hash da tabela.
 */
public class Aluno {

    private final int matricula;
    private final String nome;
    private final int idade;

    public Aluno(int matricula, String nome, int idade) {
        this.matricula = matricula;
        this.nome = nome;
        this.idade = idade;
    }

    public int getMatricula() { return matricula; }
    public String getNome()   { return nome; }
    public int getIdade()     { return idade; }

    /** Formato usado na exibição da tabela: [1001 | João | 20] */
    @Override
    public String toString() {
        return "[" + matricula + " | " + nome + " | " + idade + "]";
    }
}
