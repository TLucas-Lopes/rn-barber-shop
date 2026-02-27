// ===================== CONFIG =====================
const PORTA = 9095;
const BASE_URL = `http://localhost:${PORTA}`;

const API_CONFIG = `${BASE_URL}/admin/config`;
const API_HORARIOS = `${BASE_URL}/horarios/disponiveis`;
const API_AG = `${BASE_URL}/agendamentos`;
const API_BLOQ = `${BASE_URL}/admin/bloqueios`;

// serviços: admin (CRUD) e cliente (somente ativos)
const API_ADMIN_SERVICOS = `${BASE_URL}/admin/servicos`;
const API_SERVICOS_PUBLICO = `${BASE_URL}/servicos`;

// Relatórios
const API_REL_MENSAL = `${BASE_URL}/admin/relatorios/mensal`;
const API_RELATORIO_SEMANAL = `${BASE_URL}/admin/relatorios/semanal`;

// Trocar senha
const API_TROCAR_SENHA = `${BASE_URL}/admin/usuarios/trocar-senha`;

const $ = (id) => document.getElementById(id);

// ✅ WhatsApp fixo da barbearia (só números com DDD)
const WHATS_BARBEARIA = "5577988185400";

// ===================== UI HELPERS =====================
function toast(msg, tipo = "ok") {
  const t = $("toast");
  if (!t) return;

  t.textContent = msg;
  t.className = `toast ${tipo}`;
  t.classList.remove("hidden");

  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => t.classList.add("hidden"), 2500);
}

function setDisabledSalvarConfig(on) {
  const btn = document.querySelector("#sec-config .btn");
  if (btn) btn.disabled = on;
}

function formatarDataHora(iso) {
  if (!iso) return "-";
  return iso.replace("T", " ").slice(0, 16);
}

function soHora(iso) {
  if (!iso) return "-";
  const t = iso.split("T")[1] || "";
  return t.slice(0, 5);
}

function escapeJs(str) {
  return String(str ?? "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

function escapeHtml(str) {
  return String(str ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

// ===================== WHATSAPP (ADMIN) =====================
function getWhatsBarbearia() {
  return String(WHATS_BARBEARIA || "").replace(/\D/g, "");
}

function abrirWhatsAgendamentoAdmin({ id, cliente, servico, dataHora }) {
  const numero = getWhatsBarbearia();
  if (!numero) {
    toast("WhatsApp da barbearia não configurado.", "err");
    return;
  }

  const texto =
    `Olá! Agendamento confirmado ✅\n` +
    `Cliente: ${cliente}\n` +
    `Serviço: ${servico}\n` +
    `Data/Hora: ${formatarDataHora(dataHora)}\n` +
    `ID: ${id}`;

  const url = `https://wa.me/${numero}?text=${encodeURIComponent(texto)}`;
  window.open(url, "_blank");
}

// ===================== AUTOSAVE (DEBOUNCE) =====================
let autosaveTimer = null;
let lastToastAt = 0;

function autosaveConfig() {
  clearTimeout(autosaveTimer);
  autosaveTimer = setTimeout(() => salvarConfig(true), 600);
}

function toastSuave(msg = "Salvo ✅") {
  const now = Date.now();
  if (now - lastToastAt < 900) return;
  lastToastAt = now;
  toast(msg, "ok");
}

// ===================== CONFIG (FUNCIONAMENTO) =====================
function preencherTelaConfig(cfg) {
  $("seg").checked = !!cfg.seg;
  $("ter").checked = !!cfg.ter;
  $("qua").checked = !!cfg.qua;
  $("qui").checked = !!cfg.qui;
  $("sex").checked = !!cfg.sex;
  $("sab").checked = !!cfg.sab;
  $("dom").checked = !!cfg.dom;

  $("intervaloMin").value = cfg.intervaloMin ?? 30;
  $("iniManha").value = cfg.iniManha || "08:00";
  $("fimManha").value = cfg.fimManha || "12:00";
  $("iniTarde").value = cfg.iniTarde || "14:00";
  $("fimTarde").value = cfg.fimTarde || "19:00";
}

function montarPayloadConfig() {
  return {
    id: 1,
    intervaloMin: Number($("intervaloMin").value || 30),

    seg: $("seg").checked,
    ter: $("ter").checked,
    qua: $("qua").checked,
    qui: $("qui").checked,
    sex: $("sex").checked,
    sab: $("sab").checked,
    dom: $("dom").checked,

    iniManha: $("iniManha").value,
    fimManha: $("fimManha").value,
    iniTarde: $("iniTarde").value,
    fimTarde: $("fimTarde").value,
  };
}

async function carregarConfig() {
  try {
    setDisabledSalvarConfig(true);

    const res = await fetch(API_CONFIG);
    if (!res.ok) {
      toast("Falha ao carregar config: " + (await res.text()), "err");
      return;
    }

    const cfg = await res.json();
    preencherTelaConfig(cfg);
  } catch (e) {
    toast("Erro ao carregar config ❌", "err");
  } finally {
    setDisabledSalvarConfig(false);
  }
}

async function salvarConfig(silencioso = false) {
  try {
    setDisabledSalvarConfig(true);

    const payload = montarPayloadConfig();

    const res = await fetch(API_CONFIG, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      toast("Falha ao salvar: " + (await res.text()), "err");
      return;
    }

    const cfgSalvo = await res.json();
    preencherTelaConfig(cfgSalvo);

    if (!silencioso) toast("Config salva ✅", "ok");
  } catch (e) {
    toast("Erro na requisição ❌", "err");
  } finally {
    setDisabledSalvarConfig(false);
  }
}

window.carregarConfig = carregarConfig;
window.salvarConfig = salvarConfig;

function ligarAutosaveConfig() {
  const ids = [
    "seg", "ter", "qua", "qui", "sex", "sab", "dom",
    "intervaloMin", "iniManha", "fimManha", "iniTarde", "fimTarde",
  ];

  ids.forEach((id) => {
    const el = $(id);
    if (!el) return;

    el.addEventListener("change", () => {
      autosaveConfig();
      toastSuave("Salvo ✅");
    });

    el.addEventListener("input", () => autosaveConfig());
  });
}

// ===================== BLOQUEIOS =====================
async function carregarBloqueios() {
  try {
    const res = await fetch(API_BLOQ);
    if (!res.ok) {
      toast("Erro ao carregar bloqueios: " + (await res.text()), "err");
      return;
    }

    const lista = await res.json();
    const tbody = $("tbodyBloqueios");
    if (!tbody) return;

    tbody.innerHTML = (lista || []).map((b) => {
      const data = b.data || b.dia || b.date || "";
      const id = b.id;

      return `
        <tr>
          <td>${escapeHtml(data)}</td>
          <td>
            ${
              id != null
                ? `<button class="btn2 danger" type="button" onclick="desbloquearDiaPorId(${Number(id)})">Desbloquear</button>`
                : `<button class="btn2 danger" type="button" onclick="desbloquearDiaPorData('${escapeJs(data)}')">Desbloquear</button>`
            }
          </td>
        </tr>
      `;
    }).join("");

    const contador = $("contadorBloqueios");
    if (contador) contador.textContent = (lista || []).length;
  } catch (e) {
    toast("Falha ao buscar bloqueios ❌", "err");
  }
}

/**
 * backend esperando /admin/bloqueios?data=YYYY-MM-DD
 */
async function bloquearDia() {
  const data = $("dataBloqueio")?.value;
  if (!data) {
    toast("Selecione uma data.", "err");
    return;
  }

  try {
    const res = await fetch(`${API_BLOQ}?data=${encodeURIComponent(data)}`, {
      method: "POST",
    });

    if (!res.ok) {
      const txt = await res.text();
      console.error("Erro ao bloquear:", res.status, txt);
      toast("Erro ao bloquear: " + txt, "err");
      return;
    }

    toast("Dia bloqueado ✅", "ok");
    $("dataBloqueio").value = "";
    await carregarBloqueios();
  } catch (e) {
    console.error(e);
    toast("Falha de conexão ao bloquear ❌", "err");
  }
}

async function desbloquearDiaPorId(id) {
  try {
    const res = await fetch(`${API_BLOQ}/${encodeURIComponent(id)}`, { method: "DELETE" });

    if (!res.ok) {
      const txt = await res.text();
      console.error("Erro ao desbloquear (id):", res.status, txt);
      toast("Erro ao desbloquear: " + txt, "err");
      return;
    }

    toast("Dia desbloqueado ✅", "ok");
    await carregarBloqueios();
  } catch (e) {
    console.error(e);
    toast("Falha de conexão ao desbloquear ❌", "err");
  }
}

async function desbloquearDiaPorData(data) {
  if (!data) return;
  try {
    const res = await fetch(`${API_BLOQ}?data=${encodeURIComponent(data)}`, { method: "DELETE" });

    if (!res.ok) {
      const txt = await res.text();
      console.error("Erro ao desbloquear (data):", res.status, txt);
      toast("Erro ao desbloquear: " + txt, "err");
      return;
    }

    toast("Dia desbloqueado ✅", "ok");
    await carregarBloqueios();
  } catch (e) {
    console.error(e);
    toast("Falha de conexão ao desbloquear ❌", "err");
  }
}

window.bloquearDia = bloquearDia;
window.desbloquearDiaPorId = desbloquearDiaPorId;
window.desbloquearDiaPorData = desbloquearDiaPorData;

// ===================== AGENDA DO DIA =====================
async function carregarAgendaDia() {
  const data = $("dataDia")?.value;
  if (!data) {
    toast("Escolha uma data.", "err");
    return;
  }

  const tbody = $("tbodyDia");
  const contador = $("contadorDia");
  if (tbody) tbody.innerHTML = "";

  try {
    let lista = null;

    let res = await fetch(`${API_AG}?data=${encodeURIComponent(data)}`);
    if (res.ok) {
      lista = await res.json();
    } else {
      const resAll = await fetch(API_AG);
      if (!resAll.ok) {
        toast("Falha ao carregar agenda: " + (await resAll.text()), "err");
        return;
      }
      const all = await resAll.json();
      lista = (all || []).filter((a) => (a.dataHora || "").startsWith(data));
    }

    if (!Array.isArray(lista)) lista = [];
    if (contador) contador.textContent = lista.length;

    lista.forEach((a) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${a.id}</td>
        <td>${escapeHtml(a.cliente)}</td>
        <td>${escapeHtml(a.servico || a.servicoNome || "-")}</td>
        <td>${escapeHtml(soHora(a.dataHora))}</td>
        <td>
          <div class="actions">
            <button class="danger" type="button" onclick="excluirAgendamento(${a.id})">Excluir</button>
            <button class="btn2" type="button"
              onclick="whatsAg(${a.id}, '${escapeJs(a.cliente)}', '${escapeJs(a.servico || a.servicoNome || "")}', '${escapeJs(a.dataHora)}')">
              WhatsApp
            </button>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (e) {
    toast("Erro ao carregar agenda ❌", "err");
  }
}

async function excluirAgendamento(id) {
  const ok = confirm(`Excluir agendamento #${id}?`);
  if (!ok) return;

  try {
    const res = await fetch(`${API_AG}/${id}`, { method: "DELETE" });

    if (res.status !== 204 && !res.ok) {
      toast("Erro ao excluir: " + (await res.text()), "err");
      return;
    }

    toast("Excluído ✅", "ok");
    await carregarAgendaDia();
    await carregarHorariosReserva();
  } catch (e) {
    toast("Falha ao excluir ❌", "err");
  }
}

function whatsAg(id, cliente, servico, dataHora) {
  abrirWhatsAgendamentoAdmin({ id, cliente, servico, dataHora });
}

window.carregarAgendaDia = carregarAgendaDia;
window.excluirAgendamento = excluirAgendamento;
window.whatsAg = whatsAg;

// ===================== SERVIÇOS (CRUD ADMIN) =====================
function lerPrecoInput(v) {
  if (v == null) return NaN;
  const s = String(v).trim().replace(",", ".");
  return Number(s);
}

function getValorServico(s) {
  if (s?.valor != null) return lerPrecoInput(s.valor);
  if (s?.preco != null) return lerPrecoInput(s.preco);
  return 0;
}

function fmtBRL(n) {
  const num = Number(n);
  if (!Number.isFinite(num)) return "0.00";
  return num.toFixed(2);
}

async function carregarServicosTabela() {
  const tbody = $("tbodyServicos");
  const contador = $("contadorServicos");
  if (!tbody) return;

  try {
    const res = await fetch(API_ADMIN_SERVICOS);
    if (!res.ok) {
      toast("Erro ao carregar serviços: " + (await res.text()), "err");
      tbody.innerHTML = "";
      if (contador) contador.textContent = "0";
      return;
    }

    const lista = await res.json();
    const arr = Array.isArray(lista) ? lista : [];

    if (contador) contador.textContent = arr.length;
    tbody.innerHTML = "";

    arr
      .sort((a, b) => (a.id ?? 0) - (b.id ?? 0))
      .forEach((s) => {
        const id = s.id;
        const nome = s.nome ?? "";
        const valor = getValorServico(s);
        const ativo = s.ativo == null ? true : !!s.ativo;

        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${id}</td>
          <td><input id="srvNome_${id}" value="${escapeHtml(nome)}" style="width:100%;" /></td>
          <td><input id="srvValor_${id}" type="text" value="${fmtBRL(valor)}" style="width:110px;" /></td>
          <td style="text-align:center;">
            <input id="srvAtivo_${id}" type="checkbox" ${ativo ? "checked" : ""} />
          </td>
          <td>
            <div class="actions">
              <button class="btn2" type="button" onclick="salvarServico(${id})">Salvar</button>
              <button class="danger" type="button" onclick="excluirServico(${id})">Excluir</button>
            </div>
          </td>
        `;
        tbody.appendChild(tr);
      });
  } catch (e) {
    toast("Falha ao carregar serviços ❌", "err");
  }
}

async function criarServico() {
  const nomeEl = $("srvNome");
  const valorEl = $("srvPreco");

  if (!nomeEl || !valorEl) {
    toast("Seção de serviços não está no HTML.", "err");
    return;
  }

  const nome = nomeEl.value.trim();
  const valor = lerPrecoInput(valorEl.value);

  if (!nome) return toast("Informe o nome do serviço.", "err");
  if (!Number.isFinite(valor) || valor < 0) return toast("Informe um preço válido.", "err");

  try {
    const payload = { nome, valor, ativo: true };

    const res = await fetch(API_ADMIN_SERVICOS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      toast("Erro ao criar: " + (await res.text()), "err");
      return;
    }

    toast("Serviço criado ✅", "ok");
    nomeEl.value = "";
    valorEl.value = "";

    await carregarServicosTabela();
    await carregarServicosAdminSelect();
  } catch (e) {
    toast("Falha na requisição ❌", "err");
  }
}

async function salvarServico(id) {
  const nome = $("srvNome_" + id)?.value?.trim();
  const valor = lerPrecoInput($("srvValor_" + id)?.value);
  const ativo = $("srvAtivo_" + id)?.checked ?? true;

  if (!nome) return toast("Nome inválido.", "err");
  if (!Number.isFinite(valor) || valor < 0) return toast("Preço inválido.", "err");

  try {
    const payload = { nome, valor, ativo };

    const res = await fetch(`${API_ADMIN_SERVICOS}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      toast("Erro ao salvar: " + (await res.text()), "err");
      return;
    }

    toast("Serviço atualizado ✅", "ok");
    await carregarServicosTabela();
    await carregarServicosAdminSelect();
  } catch (e) {
    toast("Falha ao salvar ❌", "err");
  }
}

async function excluirServico(id) {
  const ok = confirm(`Excluir serviço #${id}?`);
  if (!ok) return;

  try {
    const res = await fetch(`${API_ADMIN_SERVICOS}/${id}`, { method: "DELETE" });

    if (!(res.status === 204 || res.ok)) {
      toast("Erro ao excluir: " + (await res.text()), "err");
      return;
    }

    toast("Serviço excluído ✅", "ok");
    await carregarServicosTabela();
    await carregarServicosAdminSelect();
  } catch (e) {
    toast("Falha ao excluir ❌", "err");
  }
}

window.carregarServicosTabela = carregarServicosTabela;
window.criarServico = criarServico;
window.salvarServico = salvarServico;
window.excluirServico = excluirServico;

// ===================== SERVIÇOS (SELECT DA RESERVA ADMIN) =====================
async function carregarServicosAdminSelect() {
  const select = $("admServicoId");
  if (!select) return;

  async function tentar(url) {
    const r = await fetch(url);
    if (!r.ok) return null;
    const j = await r.json();
    return Array.isArray(j) ? j : null;
  }

  try {
    let lista = await tentar(API_ADMIN_SERVICOS);
    if (!lista) lista = await tentar(API_SERVICOS_PUBLICO);

    if (!lista) {
      select.innerHTML = `<option value="">Sem rota de serviços</option>`;
      toast("Rota de serviços não encontrada (/admin/servicos ou /servicos)", "err");
      return;
    }

    select.innerHTML = `<option value="">Selecione</option>`;

    lista.forEach((s) => {
      const ativo = s.ativo == null ? true : !!s.ativo;
      const nome = s.nome ?? s.descricao ?? `Serviço ${s.id}`;
      const valor = getValorServico(s);

      const opt = document.createElement("option");
      opt.value = s.id;
      opt.textContent = `${nome} (R$ ${fmtBRL(valor)})${ativo ? "" : " [INATIVO]"}`;
      select.appendChild(opt);
    });
  } catch (e) {
    select.innerHTML = `<option value="">Erro ao carregar</option>`;
    toast("Falha ao buscar serviços ❌", "err");
  }
}
window.carregarServicosAdminSelect = carregarServicosAdminSelect;

// ===================== HORÁRIOS PARA RESERVA ADMIN =====================
function limparSelectHorariosAdmin(msg = "Selecione uma data primeiro") {
  const sel = $("admHora");
  if (!sel) return;
  sel.innerHTML = `<option value="">${msg}</option>`;
}

async function carregarHorariosReserva() {
  const data = $("admData")?.value;
  const sel = $("admHora");
  if (!sel) return;

  if (!data) {
    limparSelectHorariosAdmin();
    return;
  }

  try {
    sel.disabled = true;
    sel.innerHTML = `<option value="">Carregando...</option>`;

    const res = await fetch(`${API_HORARIOS}?data=${encodeURIComponent(data)}`);
    if (!res.ok) {
      sel.innerHTML = `<option value="">Erro</option>`;
      toast("Erro ao carregar horários: " + (await res.text()), "err");
      return;
    }

    const horarios = await res.json();
    sel.innerHTML = `<option value="">Selecione</option>`;

    if (!horarios.length) {
      sel.innerHTML = `<option value="">Sem horários</option>`;
      return;
    }

    horarios.forEach((h) => {
      const opt = document.createElement("option");
      opt.value = h;
      opt.textContent = h;
      sel.appendChild(opt);
    });
  } catch (e) {
    toast("Falha ao buscar horários ❌", "err");
    sel.innerHTML = `<option value="">Erro</option>`;
  } finally {
    sel.disabled = false;
  }
}
window.carregarHorariosReserva = carregarHorariosReserva;

// ===================== RESERVA PELO ADMIN =====================
async function reservarAdmin() {
  const cliente = $("admCliente")?.value.trim();
  const servicoId = $("admServicoId")?.value;
  const data = $("admData")?.value;
  const hora = $("admHora")?.value;

  if (!cliente || !servicoId || !data || !hora) {
    toast("Preencha cliente, serviço, data e horário.", "err");
    return;
  }

  const payload = {
    cliente,
    servicoId: Number(servicoId),
    dataHora: `${data}T${hora}:00`,
  };

  try {
    const res = await fetch(API_AG, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      toast(await res.text(), "err");
      return;
    }

    toast("Reservado pelo admin ✅", "ok");

    $("admCliente").value = "";
    $("admHora").value = "";

    if ($("dataDia")?.value === data) {
      await carregarAgendaDia();
    }
    await carregarHorariosReserva();
  } catch (e) {
    toast("Erro na requisição ❌", "err");
  }
}
window.reservarAdmin = reservarAdmin;

// ===================== RELATORIO MENSAL =====================
let chartMensal = null;

async function carregarRelatorioMensal() {
  const ano = Number($("anoRelatorio")?.value || new Date().getFullYear());

  try {
    const res = await fetch(`${API_REL_MENSAL}?ano=${ano}`);
    if (!res.ok) {
      toast("Erro relatório: " + (await res.text()), "err");
      return;
    }

    const dados = await res.json();

    const meses = ["Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez"];
    const totalMes = Array(12).fill(0);
    const qtdMes = Array(12).fill(0);

    (dados || []).forEach((r) => {
      const idx = (r.mes || 1) - 1;
      qtdMes[idx] = Number(r.qtd || 0);
      totalMes[idx] = Number(r.total || 0);
    });

    const ctx = $("graficoMensal");
    if (!ctx) return;

    if (chartMensal) chartMensal.destroy();

    chartMensal = new Chart(ctx, {
      type: "bar",
      data: {
        labels: meses,
        datasets: [
          { label: "Faturamento (R$)", data: totalMes },
          { label: "Atendimentos", data: qtdMes }
        ]
      },
      options: {
        responsive: true,
        scales: { y: { beginAtZero: true } }
      }
    });

    toast("Relatório carregado ✅", "ok");
  } catch (e) {
    toast("Falha ao carregar relatório ❌", "err");
  }
}
window.carregarRelatorioMensal = carregarRelatorioMensal;

// ===================== RELATORIO SEMANAL =====================
let chartSemanal = null;

async function carregarRelatorioSemanal() {
  const canvas = $("graficoSemanal");
  const resumoEl = $("resumoSemanal");

  if (!canvas) {
    toast("Canvas do gráfico semanal não encontrado (graficoSemanal).", "err");
    return;
  }

  try {
    const res = await fetch(API_RELATORIO_SEMANAL);

    if (!res.ok) {
      const txt = await res.text();
      toast("Erro no relatório semanal: " + txt, "err");
      return;
    }

    const data = await res.json();

    const labels = Array.isArray(data.labels) ? data.labels : [];
    const valores = Array.isArray(data.valores) ? data.valores : [];
    const qtd = Array.isArray(data.qtd) ? data.qtd : [];

    if (!labels.length || !valores.length) {
      toast("Relatório semanal veio vazio.", "err");
      if (chartSemanal) {
        chartSemanal.destroy();
        chartSemanal = null;
      }
      if (resumoEl) resumoEl.textContent = "";
      return;
    }

    const total = valores.reduce((acc, v) => acc + (Number(v) || 0), 0);
    const atendimentos = qtd.length ? qtd.reduce((acc, v) => acc + (Number(v) || 0), 0) : null;
    const ticketMedio = atendimentos && atendimentos > 0 ? total / atendimentos : null;

    if (chartSemanal) chartSemanal.destroy();

    chartSemanal = new Chart(canvas, {
      type: "bar",
      data: {
        labels,
        datasets: [
          { label: "Faturamento (R$)", data: valores.map((v) => Number(v) || 0) },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          tooltip: {
            callbacks: {
              label: function (ctx) {
                const v = ctx.parsed.y ?? 0;
                return "R$ " + v.toFixed(2);
              },
            },
          },
        },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return "R$ " + Number(value).toFixed(0);
              },
            },
          },
        },
      },
    });

    if (resumoEl) {
      let txt = `Total da semana: R$ ${total.toFixed(2)}`;
      if (atendimentos != null) txt += ` | Atendimentos: ${atendimentos}`;
      if (ticketMedio != null) txt += ` | Ticket médio: R$ ${ticketMedio.toFixed(2)}`;
      resumoEl.textContent = txt;
    }

    toast("Relatório semanal carregado ✅", "ok");
  } catch (e) {
    toast("Falha ao carregar relatório semanal ❌", "err");
  }
}
window.carregarRelatorioSemanal = carregarRelatorioSemanal;

// ===================== TROCAR SENHA =====================
async function trocarSenhaAdmin() {
  const senhaAtual = $("senhaAtual")?.value;
  const novaSenha = $("novaSenha")?.value;
  const confirmarSenha = $("confirmarSenha")?.value;

  if (!senhaAtual || !novaSenha || !confirmarSenha) {
    toast("Preencha todos os campos.", "err");
    return;
  }

  try {
    const resp = await fetch(API_TROCAR_SENHA, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({ senhaAtual, novaSenha, confirmarSenha }),
    });

    if (resp.status === 204) {
      toast("Senha alterada com sucesso ✅", "ok");
      $("senhaAtual").value = "";
      $("novaSenha").value = "";
      $("confirmarSenha").value = "";
      return;
    }

    const err = await resp.json().catch(() => null);
    toast(err?.message || "Erro ao trocar senha.", "err");
  } catch (e) {
    toast("Falha de rede ao trocar senha.", "err");
  }
}
window.trocarSenhaAdmin = trocarSenhaAdmin;

// ===================== START =====================
document.addEventListener("DOMContentLoaded", () => {
  ligarAutosaveConfig();

  carregarConfig();
  carregarBloqueios();

  if ($("admData")) $("admData").addEventListener("change", carregarHorariosReserva);

  carregarServicosTabela();
  carregarServicosAdminSelect();

  limparSelectHorariosAdmin();
});