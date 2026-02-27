// ===================== CONFIG =====================
const PORTA = 9095;

const API_AG = `http://localhost:${PORTA}/agendamentos`;
const API_HORARIOS = `http://localhost:${PORTA}/horarios/disponiveis`;
const API_SERVICOS = `http://localhost:${PORTA}/servicos`;

const WHATS_BARBEARIA = "77988185400"; // ✅ WhatsApp fixo da barbearia

const $ = (id) => document.getElementById(id);

// ===================== DATA LIMITADA (7 DIAS) =====================
function limitarDataCliente() {
  const inp = document.getElementById("data");
  if (!inp) return;

  const hoje = new Date();
  const min = hoje.toISOString().slice(0, 10);

  const limite = new Date();
  limite.setDate(hoje.getDate() + 6);
  const max = limite.toISOString().slice(0, 10);

  inp.min = min;
  inp.max = max;
}
limitarDataCliente();

// ===================== UI HELPERS =====================
function setLoading(on) {
  const el = $("loader");
  if (!el) return;
  el.classList.toggle("hidden", !on);
}

function toast(texto, tipo = "ok") {
  const t = $("toast");
  if (!t) return;
  t.className = "toast " + (tipo === "ok" ? "ok" : "err");
  t.textContent = texto;
  t.classList.remove("hidden");
  setTimeout(() => t.classList.add("hidden"), 2500);
}

function formatarData(iso) {
  if (!iso) return "-";
  return iso.replace("T", " ").slice(0, 16);
}

function escapeJs(str) {
  return String(str ?? "").replace(/\\/g, "\\\\").replace(/'/g, "\\'");
}

// ===================== SERVIÇOS =====================
async function carregarServicos() {
  const sel = document.getElementById("servicoId");
  if (!sel) return;

  try {
    const res = await fetch(API_SERVICOS);
    if (!res.ok) {
      sel.innerHTML = `<option value="">Erro ao carregar serviços</option>`;
      return;
    }

    const lista = await res.json();
    sel.innerHTML = `<option value="">Selecione um serviço</option>`;

    (lista || []).forEach((s) => {
      const id = s.id;

      // ✅ valor correto do backend
      const valorRaw = s.valor ?? s.preco ?? 0;

      // ✅ garante número mesmo que venha string
      const valorNum = Number(String(valorRaw).replace(",", "."));

      const nome = s.nome ?? "Serviço";
      const precoTxt = Number.isFinite(valorNum) ? valorNum.toFixed(2) : "0.00";

      const opt = document.createElement("option");
      opt.value = id;
      opt.textContent = `${nome} - R$ ${precoTxt}`;
      sel.appendChild(opt);
    });
  } catch (e) {
    sel.innerHTML = `<option value="">Falha ao carregar serviços</option>`;
  }
}

// ===================== HORÁRIOS =====================
function limparHorarios() {
  const select = $("hora");
  if (!select) return;
  select.innerHTML = `<option value="">Selecione um horário</option>`;
}

async function carregarHorarios() {
  const data = $("data")?.value;
  const select = $("hora");

  if (!select) return;
  limparHorarios();
  if (!data) return;

  try {
    select.disabled = true;

    const res = await fetch(`${API_HORARIOS}?data=${encodeURIComponent(data)}`);
    if (!res.ok) {
      toast("Erro ao carregar horários: " + (await res.text()), "err");
      return;
    }

    const horarios = await res.json();
    if (!horarios.length) {
      toast("Sem horários disponíveis nesse dia.", "err");
      return;
    }

    horarios.forEach((h) => {
      const opt = document.createElement("option");
      opt.value = h;
      opt.textContent = h;
      select.appendChild(opt);
    });
  } catch (e) {
    toast("Falha ao buscar horários ❌", "err");
  } finally {
    select.disabled = false;
  }
}

// ===================== LISTA (SE EXISTIR NO HTML) =====================
// OBS: Se você remover a parte "Agendamentos feitos" do HTML, esse bloco
// não quebra nada: ele só não vai fazer nada se não encontrar tbody/contador.
async function carregar() {
  const tbody = $("tbody");
  const contador = $("contador");

  // se não existir na tela, não carrega lista
  if (!tbody || !contador) return;

  try {
    setLoading(true);

    const res = await fetch(API_AG);
    const dados = await res.json();

    contador.textContent = (dados || []).length;
    tbody.innerHTML = "";

    (dados || []).forEach((a) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>${a.id}</td>
        <td>${a.cliente}</td>
        <td>${a.servicoNome ?? "-"}</td>
        <td>${formatarData(a.dataHora)}</td>
        <td>
          <div class="actions">
            <button class="danger" onclick="excluir(${a.id})">Excluir</button>
            <button class="btn2" onclick="whats(${a.id}, '${escapeJs(a.cliente)}', '${escapeJs(
        a.servicoNome ?? ""
      )}', '${escapeJs(a.dataHora)}')">WhatsApp</button>
          </div>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (e) {
    toast("Erro ao carregar lista ❌", "err");
  } finally {
    setLoading(false);
  }
}

// ===================== CRIAR (AGENDAR) + WHATSAPP AUTOMÁTICO =====================
async function criar() {
  const cliente = $("cliente")?.value?.trim();
  const servicoId = $("servicoId")?.value;

  const data = $("data")?.value;
  const hora = $("hora")?.value;

  if (!cliente || !servicoId || !data || !hora) {
    toast("Preencha cliente, serviço, data e horário.", "err");
    return;
  }

  // pega o texto do serviço selecionado (ex: "Corte - R$ 25.00")
  const servicoSelect = $("servicoId");
  const servicoNome = servicoSelect?.options?.[servicoSelect.selectedIndex]?.text || "";

  const payload = {
    cliente,
    servicoId: Number(servicoId),
    dataHora: `${data}T${hora}:00`,
  };

  try {
    setLoading(true);

    const res = await fetch(API_AG, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload),
    });

    if (!res.ok) {
      let msg = "Erro ao salvar";
      try {
        const j = await res.json();
        msg = j.message || msg;
      } catch {
        msg = await res.text();
      }
      toast(msg, "err");
      return;
    }

    toast("Agendamento criado ✅", "ok");

    // ✅ Abre WhatsApp da barbearia automaticamente
    const msg =
      `Olá! Acabei de fazer um agendamento 💈%0A%0A` +
      `👤 Cliente: ${encodeURIComponent(cliente)}%0A` +
      `✂️ Serviço: ${encodeURIComponent(servicoNome)}%0A` +
      `📅 Data: ${encodeURIComponent(data)}%0A` +
      `⏰ Hora: ${encodeURIComponent(hora)}%0A%0A` +
      `Aguardo confirmação. Obrigado!`;

    window.open(`https://wa.me/${WHATS_BARBEARIA}?text=${msg}`, "_blank");

    // limpa campos
    if ($("cliente")) $("cliente").value = "";
    if ($("servicoId")) $("servicoId").value = "";
    if ($("hora")) $("hora").value = "";

    // recarrega horários do dia selecionado (pra refletir o horário ocupado)
    await carregarHorarios();

    // se a lista existir (se você ainda tiver no HTML), atualiza
    await carregar();
  } catch (e) {
    toast("Falha na requisição ❌", "err");
  } finally {
    setLoading(false);
  }
}

// ===================== EXCLUIR (SE EXISTIR LISTA) =====================
async function excluir(id) {
  const ok = confirm(`Excluir agendamento #${id}?`);
  if (!ok) return;

  try {
    setLoading(true);

    const res = await fetch(`${API_AG}/${id}`, { method: "DELETE" });

    if (res.status !== 204) {
      toast("Erro ao excluir: " + (await res.text()), "err");
      return;
    }

    toast("Agendamento excluído ✅", "ok");
    await carregar();
    await carregarHorarios();
  } catch (e) {
    toast("Falha ao excluir ❌", "err");
  } finally {
    setLoading(false);
  }
}

// ===================== WHATS (LISTA) =====================
function whats(id, cliente, servico, dataHora) {
  // mantém para uso interno/adm se você ainda usar a lista
  const msg =
    `Olá! Agendamento confirmado ✅%0A` +
    `Cliente: ${cliente}%0A` +
    `Serviço: ${servico}%0A` +
    `Data/Hora: ${formatarData(dataHora)}%0A` +
    `ID: ${id}`;

  window.open(`https://wa.me/${WHATS_BARBEARIA}?text=${msg}`, "_blank");
}

// ===================== LISTENERS =====================
if ($("data")) $("data").addEventListener("change", carregarHorarios);

// primeira carga
carregarServicos();
carregarHorarios(); // opcional: só vai carregar se já tiver data
carregar(); // só roda se existir tbody/contador