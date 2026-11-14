const $ = (id) => document.getElementById(id);
const TELAS = ['inserir', 'buscar', 'remover', 'exibir', 'encerrar'];
let tabelaAtual = null;

async function chamar(url, opcoes) {
  try {
    const r = await fetch(url, opcoes);
    return { status: r.status, dados: await r.json() };
  } catch (e) {
    mostrar('erro', ['Não foi possível falar com o servidor. Ele está rodando (infralab.view.MainWeb)?']);
    return null;
  }
}

function mostrar(tipo, linhas) {
  const msg = $('msg');
  msg.className = tipo;
  msg.replaceChildren(...linhas.map((texto) => {
    const p = document.createElement('p');
    p.textContent = texto;
    return p;
  }));
}

// ---------- menu ----------
function irPara(tela) {
  TELAS.forEach((t) => {
    $(`tela-${t}`).hidden = t !== tela;
    document.querySelector(`[data-tela="${t}"]`).classList.toggle('ativo', t === tela);
  });
  mostrar('', []);
  if (tela === 'exibir') carregar();
  const campo = $(`tela-${tela}`).querySelector('input');
  if (campo) campo.focus();
}
document.querySelectorAll('[data-tela]').forEach((b) =>
  b.addEventListener('click', () => irPara(b.dataset.tela)));

// ---------- exibir tabela ----------
async function carregar() {
  const r = await chamar('/api/tabela');
  if (!r) return;
  tabelaAtual = r.dados;
  const n = tabelaAtual.quantidade;
  $('total').textContent = `(${n} ${n === 1 ? 'aluno' : 'alunos'})`;
  $('posicoes').replaceChildren(...tabelaAtual.posicoes.map(linha));
}

function linha(p) {
  const li = document.createElement('li');
  li.className = 'posicao';

  const indice = document.createElement('span');
  indice.className = 'indice';
  indice.textContent = p.indice;
  indice.title = `Índice ${p.indice}`;

  const cadeia = document.createElement('div');
  cadeia.className = 'cadeia';
  if (p.alunos.length === 0) {
    const vazio = document.createElement('span');
    vazio.className = 'vazio';
    vazio.textContent = 'Vazio';
    cadeia.append(vazio);
  }
  p.alunos.forEach((a) => {
    const no = document.createElement('span');
    no.className = 'no';
    no.textContent = `${a.matricula} | ${a.nome} | ${a.idade}`; // textContent evita injeção de HTML
    cadeia.append(no);
  });

  li.append(indice, cadeia);
  if (p.alunos.length > 1) {
    const aviso = document.createElement('span');
    aviso.className = 'colisao';
    aviso.textContent = `Colisão: ${p.alunos.length} alunos`;
    li.append(aviso);
  }
  return li;
}

// ---------- inserir ----------
$('tela-inserir').addEventListener('submit', async (e) => {
  e.preventDefault();
  const corpo = new URLSearchParams({
    matricula: $('matricula').value, nome: $('nome').value, idade: $('idade').value,
  });
  const r = await chamar('/api/alunos', { method: 'POST', body: corpo });
  if (!r) return;
  const d = r.dados;
  if (!d.resultado) return mostrar('erro', [d.erro]);

  if (d.resultado === 'DUPLICADO') {
    mostrar('erro', [`Matrícula ${d.matricula} já cadastrada. Nada foi inserido.`]);
  } else {
    mostrar('ok', [d.resultado === 'INSERIDO_COM_COLISAO'
      ? `Colisão no índice ${d.indice}: aluno inserido no final da lista.`
      : `Aluno inserido no índice ${d.indice} (posição estava vazia).`]);
    e.target.reset();
    $('matricula').focus();
  }
});

// ---------- buscar e remover ----------
async function operar(metodo, campo) {
  const r = await chamar(`/api/alunos/${encodeURIComponent(campo.value.trim())}`, { method: metodo });
  if (!r) return;
  const d = r.dados;
  if (d.indice === undefined) return mostrar('erro', [d.erro]);

  if (!d.aluno) {
    mostrar('aviso', [d.erro]);
  } else if (metodo === 'GET') {
    mostrar('ok', [`Encontrado: matrícula ${d.aluno.matricula}, nome ${d.aluno.nome}, idade ${d.aluno.idade}.`]);
  } else {
    mostrar('ok', [`Aluno removido: ${d.aluno.matricula} | ${d.aluno.nome} | ${d.aluno.idade}`]);
    campo.value = '';
  }
}
$('tela-buscar').addEventListener('submit', (e) => { e.preventDefault(); operar('GET', $('busca')); });
$('tela-remover').addEventListener('submit', (e) => { e.preventDefault(); operar('DELETE', $('remocao')); });

// ---------- encerrar ----------
$('btn-encerrar').addEventListener('click', async () => {
  const r = await chamar('/api/encerrar', { method: 'POST' });
  if (!r) return;
  document.querySelectorAll('button, input').forEach((el) => { el.disabled = true; });
  mostrar('aviso', ['Sistema encerrado. Até logo! Você já pode fechar esta aba.']);
});
