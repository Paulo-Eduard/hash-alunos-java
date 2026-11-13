# Sistema de Gerenciamento de Alunos com Tabela Hash

## Objetivo
Sistema acadêmico de console para cadastrar, buscar, remover e visualizar alunos, usando uma **Tabela Hash com Encadeamento (Separate Chaining)** implementada manualmente (sem `HashMap`, `Hashtable`, `HashSet` ou `LinkedList`). Trabalho da disciplina de Construção de Compiladores.

## A Tabela Hash
Uma Tabela Hash é um vetor em que a posição de cada registro é calculada a partir da sua **chave**, o que permite acessar o registro sem percorrer a estrutura inteira. Aqui:

- 11 posições, índices de 0 a 10.
- Chave = matrícula do aluno (inteiro positivo).
- Cada posição guarda a referência para o início de uma **lista simplesmente encadeada**.

## Função Hash
```
h(chave) = chave % 11
```
Exemplo: `h(1023) = 1023 % 11 = 0` → o aluno vai para o índice 0.

## Colisão e encadeamento
Uma **colisão** ocorre quando matrículas diferentes geram o mesmo índice (ex.: 1001, 1012 e 1023 resultam todas em 0). Em vez de procurar outra posição, os alunos ficam **na mesma lista**:

```
ÍNDICE 0 → [1001 | João | 20] → [1012 | Maria | 22] → [1023 | Pedro | 19]
```

Regras implementadas:
- **Inserção sempre no final** da lista (nunca no início).
- **Matrícula única**: como a mesma matrícula sempre cai no mesmo índice, basta percorrer a lista desse índice para garantir unicidade em toda a tabela.
- **Busca**: calcula o índice e percorre somente aquela lista, comparando matrículas.
- **Remoção**: percorre a lista guardando o nó anterior.
  - primeiro nó → a cabeça da lista passa a ser o próximo;
  - intermediário/último → `anterior.proximo = atual.proximo`.

## Tecnologias
Java 17+ (testado com Java 21), orientação a objetos, console. Nenhuma biblioteca externa.

## Estrutura de pastas
```
src/
├── main/java/infralab/
│   ├── Main.java                  # inicialização
│   ├── model/Aluno.java           # matrícula, nome, idade
│   ├── structures/No.java         # nó da lista encadeada
│   ├── structures/TabelaHash.java # tabela hash e operações
│   └── view/Menu.java             # interação com o usuário
└── test/java/infralab/
    └── TabelaHashTest.java        # testes (sem JUnit)
```

## Como executar
Na raiz do projeto:

**Linux / macOS**
```
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main -name "*.java")
java -cp out infralab.Main
```

**Windows (PowerShell)**
```
mkdir out
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src\main -Filter *.java).FullName
chcp 65001
java -cp out infralab.Main
```

**Testes**
```
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out infralab.TabelaHashTest
```

No Eclipse/VS Code: importe como projeto Java com `src/main/java` e `src/test/java` como pastas de código-fonte e execute `infralab.Main`.

## Exemplo de utilização
Inserir 1001/João/20, 1012/Maria/22 e 1023/Pedro/19, depois escolher a opção 4:
```
ÍNDICE  0 → [1001 | João | 20] → [1012 | Maria | 22] → [1023 | Pedro | 19]   <== COLISÃO (3 alunos)
ÍNDICE  1 → Vazio
...
```

## Casos de teste (49 verificações em `TabelaHashTest`)
1. Cadastro de um aluno · 2. Vários alunos · 3. Inserção com colisão · 4. Inserção no final · 5. Matrícula duplicada (cabeça e último nó) · 6. Busca existente · 7. Busca inexistente · 8. Remoção do primeiro nó · 9. Remoção intermediária · 10. Remoção do último nó (e do único nó) · 11. Remoção inexistente · 12. Exibição das 11 posições · 13. Entradas inválidas (texto, vazio, negativo, zero, overflow, idade fora de 1–120) · 14. Fluxo completo pelo Menu.
