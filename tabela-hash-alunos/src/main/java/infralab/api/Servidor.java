package infralab.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import infralab.model.Aluno;
import infralab.structures.TabelaHash;
import infralab.structures.TabelaHash.ResultadoInsercao;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Servidor HTTP (apenas JDK) que expõe a TabelaHash ao frontend da pasta web/.
 * Requisições de entrada: formulário (application/x-www-form-urlencoded). Respostas: JSON.
 * O executor padrão atende uma requisição por vez, então a TabelaHash não precisa de sincronização.
 */
public class Servidor {

    private final TabelaHash tabela;
    private final Path pastaWeb;
    private final HttpServer http;

    public Servidor(TabelaHash tabela, Path pastaWeb, int porta) throws IOException {
        this.tabela = tabela;
        this.pastaWeb = pastaWeb.toAbsolutePath().normalize();
        this.http = HttpServer.create(new InetSocketAddress("localhost", porta), 0);
        http.createContext("/api/tabela", this::tratarTabela);
        http.createContext("/api/alunos", this::tratarAlunos);
        http.createContext("/api/encerrar", this::tratarEncerrar);
        http.createContext("/", this::tratarArquivo);
    }

    public void iniciar() { http.start(); }

    // POST /api/encerrar -> responde e desliga o servidor (opção "Encerrar Sistema" do menu)
    private void tratarEncerrar(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("POST")) {
            responder(ex, 405, erro("Método não permitido."));
            return;
        }
        responder(ex, 200, "{\"mensagem\":\"Sistema encerrado.\"}");
        new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) { /* encerra mesmo assim */ }
            http.stop(0);
            System.exit(0);
        }).start();
    }

    // GET /api/tabela -> as 11 posições com seus alunos
    private void tratarTabela(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) {
            responder(ex, 405, erro("Método não permitido."));
            return;
        }
        StringBuilder sb = new StringBuilder("{\"tamanho\":").append(TabelaHash.TAMANHO)
                .append(",\"quantidade\":").append(tabela.getQuantidade()).append(",\"posicoes\":[");
        for (int i = 0; i < TabelaHash.TAMANHO; i++) {
            if (i > 0) sb.append(',');
            sb.append("{\"indice\":").append(i).append(",\"alunos\":[");
            Aluno[] alunos = tabela.alunosNaPosicao(i);
            for (int j = 0; j < alunos.length; j++) {
                if (j > 0) sb.append(',');
                sb.append(jsonAluno(alunos[j]));
            }
            sb.append("]}");
        }
        responder(ex, 200, sb.append("]}").toString());
    }

    // POST /api/alunos | GET /api/alunos/{matricula} | DELETE /api/alunos/{matricula}
    private void tratarAlunos(HttpExchange ex) throws IOException {
        try {
            String metodo = ex.getRequestMethod();
            String resto = ex.getRequestURI().getPath().substring("/api/alunos".length());
            if (resto.startsWith("/")) resto = resto.substring(1);

            if (metodo.equals("POST") && resto.isEmpty()) {
                inserir(ex);
                return;
            }
            Integer matricula = inteiro(resto, 1, Integer.MAX_VALUE);
            if (matricula == null) {
                responder(ex, 400, erro("Matrícula inválida: digite um número inteiro maior ou igual a 1."));
            } else if (metodo.equals("GET")) {
                buscar(ex, matricula);
            } else if (metodo.equals("DELETE")) {
                remover(ex, matricula);
            } else {
                responder(ex, 405, erro("Método não permitido."));
            }
        } catch (Exception e) {
            responder(ex, 500, erro("Erro interno: " + e.getMessage()));
        }
    }

    private void inserir(HttpExchange ex) throws IOException {
        String corpo = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Integer matricula = inteiro(campo(corpo, "matricula"), 1, Integer.MAX_VALUE);
        Integer idade = inteiro(campo(corpo, "idade"), 1, 120);
        String nome = campo(corpo, "nome");
        nome = (nome == null) ? "" : nome.trim();

        if (matricula == null) {
            responder(ex, 400, erro("Matrícula inválida: digite um número inteiro maior ou igual a 1."));
        } else if (nome.isEmpty()) {
            responder(ex, 400, erro("O nome não pode ser vazio."));
        } else if (idade == null) {
            responder(ex, 400, erro("Idade inválida: digite um número de 1 a 120."));
        } else {
            int indice = tabela.hash(matricula);
            ResultadoInsercao r = tabela.inserir(new Aluno(matricula, nome, idade));
            responder(ex, r == ResultadoInsercao.DUPLICADO ? 409 : 201,
                    "{\"resultado\":\"" + r + "\",\"matricula\":" + matricula + ",\"indice\":" + indice + "}");
        }
    }

    private void buscar(HttpExchange ex, int matricula) throws IOException {
        Aluno a = tabela.buscar(matricula);
        if (a == null) {
            responder(ex, 404, resposta(matricula, "\"erro\":\"Aluno com matrícula " + matricula + " não está cadastrado.\""));
        } else {
            responder(ex, 200, resposta(matricula, "\"aluno\":" + jsonAluno(a)));
        }
    }

    private void remover(HttpExchange ex, int matricula) throws IOException {
        Aluno a = tabela.remover(matricula);
        if (a == null) {
            responder(ex, 404, resposta(matricula, "\"erro\":\"Matrícula " + matricula + " não encontrada. Nada foi removido.\""));
        } else {
            responder(ex, 200, resposta(matricula, "\"aluno\":" + jsonAluno(a)));
        }
    }

    // Arquivos estáticos (index.html, style.css, app.js) da pasta web/
    private void tratarArquivo(HttpExchange ex) throws IOException {
        String caminho = ex.getRequestURI().getPath();
        if (caminho.equals("/")) caminho = "/index.html";
        Path arquivo = pastaWeb.resolve(caminho.substring(1)).normalize();
        if (!arquivo.startsWith(pastaWeb) || !Files.isRegularFile(arquivo)) {
            responder(ex, 404, erro("Arquivo não encontrado."));
            return;
        }
        String nome = arquivo.getFileName().toString();
        String tipo = nome.endsWith(".html") ? "text/html; charset=utf-8"
                : nome.endsWith(".css") ? "text/css; charset=utf-8"
                : nome.endsWith(".js") ? "text/javascript; charset=utf-8"
                : "application/octet-stream";
        enviar(ex, 200, tipo, Files.readAllBytes(arquivo));
    }

    // ---------- utilitários ----------

    private String resposta(int matricula, String resto) {
        return "{\"matricula\":" + matricula + ",\"indice\":" + tabela.hash(matricula) + "," + resto + "}";
    }

    private static String campo(String corpo, String nome) {
        for (String par : corpo.split("&")) {
            String[] kv = par.split("=", 2);
            if (kv.length == 2 && kv[0].equals(nome)) {
                return URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /** Converte para inteiro no intervalo [min, max]; devolve null se for inválido. */
    private static Integer inteiro(String s, int min, int max) {
        try {
            int v = Integer.parseInt(s.trim());
            return (v >= min && v <= max) ? v : null;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String jsonAluno(Aluno a) {
        return "{\"matricula\":" + a.getMatricula() + ",\"nome\":\"" + escapar(a.getNome())
                + "\",\"idade\":" + a.getIdade() + "}";
    }

    private static String erro(String mensagem) {
        return "{\"erro\":\"" + escapar(mensagem) + "\"}";
    }

    private static String escapar(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '"') sb.append("\\\"");
            else if (c == '\\') sb.append("\\\\");
            else if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
            else sb.append(c);
        }
        return sb.toString();
    }

    private static void responder(HttpExchange ex, int status, String json) throws IOException {
        enviar(ex, status, "application/json; charset=utf-8", json.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviar(HttpExchange ex, int status, String tipo, byte[] dados) throws IOException {
        ex.getResponseHeaders().set("Content-Type", tipo);
        ex.getResponseHeaders().set("Cache-Control", "no-cache");
        ex.sendResponseHeaders(status, dados.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(dados);
        }
    }
}
