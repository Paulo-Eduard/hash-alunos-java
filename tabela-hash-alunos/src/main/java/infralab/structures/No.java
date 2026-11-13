package infralab.structures;

import infralab.model.Aluno;

/**
 * Nó da lista simplesmente encadeada.
 * Guarda um aluno e a referência para o próximo nó (null = fim da lista).
 */
public class No {

    private final Aluno aluno;
    private No proximo;

    public No(Aluno aluno) {
        this.aluno = aluno;
        this.proximo = null;
    }

    public Aluno getAluno()          { return aluno; }
    public No getProximo()           { return proximo; }
    public void setProximo(No prox)  { this.proximo = prox; }
}
