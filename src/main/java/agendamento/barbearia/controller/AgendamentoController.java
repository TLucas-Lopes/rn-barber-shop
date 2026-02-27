package agendamento.barbearia.controller;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import agendamento.barbearia.dto.AgendamentoRequest;
import agendamento.barbearia.entity.Agendamento;
import agendamento.barbearia.entity.FuncionamentoConfig;
import agendamento.barbearia.entity.Servico;
import agendamento.barbearia.repository.AgendamentoRepository;
import agendamento.barbearia.repository.DiaBloqueadoRepository;
import agendamento.barbearia.repository.FuncionamentoConfigRepository;
import agendamento.barbearia.repository.ServicoRepository;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
public class AgendamentoController {

    private final AgendamentoRepository agRepo;
    private final ServicoRepository servRepo;
    private final DiaBloqueadoRepository diaBloqRepo;
    private final FuncionamentoConfigRepository cfgRepo;

    public AgendamentoController(
            AgendamentoRepository agRepo,
            ServicoRepository servRepo,
            DiaBloqueadoRepository diaBloqRepo,
            FuncionamentoConfigRepository cfgRepo
    ) {
        this.agRepo = agRepo;
        this.servRepo = servRepo;
        this.diaBloqRepo = diaBloqRepo;
        this.cfgRepo = cfgRepo;
    }

    // =========================
    // LISTAR (com filtro opcional por data YYYY-MM-DD)
    // =========================
    @GetMapping("/agendamentos")
    public List<Agendamento> listar(@RequestParam(required = false) String data) {

        if (data == null || data.isBlank()) {
            return agRepo.findAll();
        }

        LocalDate d;
        try {
            d = LocalDate.parse(data);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parâmetro 'data' inválido (use yyyy-MM-dd).");
        }

        LocalDateTime ini = d.atStartOfDay();
        LocalDateTime fim = d.plusDays(1).atStartOfDay();

        return agRepo.findByDataHoraBetweenOrderByDataHoraAsc(ini, fim);
    }

    // =========================
    // CRIAR
    // =========================
    @PostMapping("/agendamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public Agendamento criar(@Valid @RequestBody AgendamentoRequest req) {

        LocalDateTime dataHora = parseDataHora(req.getDataHora());

        // 🔒 BLINDAGEM TOTAL (passado + janela 7 dias + bloqueado + dia ativo + faixa + intervalo)
        validarDataHoraParaAgendar(dataHora);

        // evita duplicar
        if (agRepo.existsByDataHora(dataHora)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já reservado.");
        }

        // serviço obrigatório
        if (req.getServicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "servicoId é obrigatório.");
        }

        // busca serviço
        Servico servico = servRepo.findById(req.getServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço não encontrado."));

        // checa ativo
        if (!servico.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço inativo.");
        }

        // snapshot no agendamento
        Agendamento ag = new Agendamento();
        ag.setCliente(req.getCliente());
        ag.setDataHora(dataHora);

        ag.setServicoId(servico.getId());
        ag.setServicoNome(servico.getNome());

        // no seu Servico: preço = valor
        ag.setValor(servico.getValor());

        return agRepo.save(ag);
    }

    // =========================
    // ATUALIZAR (opcional, mas blindado)
    // =========================
    @PutMapping("/agendamentos/{id}")
    public Agendamento atualizar(@PathVariable Long id, @Valid @RequestBody AgendamentoRequest req) {

        Agendamento existente = agRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Agendamento não encontrado."));

        LocalDateTime dataHora = parseDataHora(req.getDataHora());

        // 🔒 BLINDAGEM TOTAL
        validarDataHoraParaAgendar(dataHora);

        // Se mudou o horário, checa conflito
        if (!dataHora.equals(existente.getDataHora()) && agRepo.existsByDataHora(dataHora)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Horário já reservado.");
        }

        if (req.getServicoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "servicoId é obrigatório.");
        }

        Servico servico = servRepo.findById(req.getServicoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço não encontrado."));

        if (!servico.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Serviço inativo.");
        }

        existente.setCliente(req.getCliente());
        existente.setDataHora(dataHora);
        existente.setServicoId(servico.getId());
        existente.setServicoNome(servico.getNome());
        existente.setValor(servico.getValor());

        return agRepo.save(existente);
    }

    // =========================
    // EXCLUIR
    // =========================
    @DeleteMapping("/agendamentos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        if (!agRepo.existsById(id)) return;
        agRepo.deleteById(id);
    }

    // ==========================================================
    // 🔧 HELPERS (blindagem)
    // ==========================================================

    private LocalDateTime parseDataHora(String dataHoraStr) {
        if (dataHoraStr == null || dataHoraStr.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "dataHora é obrigatório.");
        }

        try {
            return LocalDateTime.parse(dataHoraStr);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dataHora inválida. Use yyyy-MM-dd'T'HH:mm:ss (ex: 2026-02-20T08:00:00)"
            );
        }
    }

    private void validarDataHoraParaAgendar(LocalDateTime dataHora) {

        // 1) não permitir passado
        if (dataHora.isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Não é permitido agendar no passado.");
        }

        // ✅ 2) janela móvel de 7 dias (hoje até hoje+6)
        LocalDate hoje = LocalDate.now();
        LocalDate limite = hoje.plusDays(6);
        LocalDate dia = dataHora.toLocalDate();

        if (dia.isBefore(hoje) || dia.isAfter(limite)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Agendamento permitido apenas para os próximos 7 dias."
            );
        }

        DayOfWeek dow = dia.getDayOfWeek();

        // 3) dia bloqueado (admin)
        if (diaBloqRepo.existsByData(dia)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dia bloqueado pela barbearia.");
        }

        // 4) carrega config (ou padrão)
        FuncionamentoConfig cfg = cfgRepo.findById(1L).orElseGet(FuncionamentoConfig::padrao);

        // 5) dia ativo no funcionamento
        if (!diaAtivo(cfg, dow)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A barbearia não atende neste dia.");
        }

        // 6) dentro das faixas (manhã ou tarde) + intervalo
        int intervalo = cfg.getIntervaloMin();
        if (intervalo <= 0) intervalo = 30;

        LocalTime hora = dataHora.toLocalTime();

        LocalTime iniManha = safeTime(cfg.getIniManha(), LocalTime.of(8, 0));
        LocalTime fimManha = safeTime(cfg.getFimManha(), LocalTime.of(12, 0));
        LocalTime iniTarde = safeTime(cfg.getIniTarde(), LocalTime.of(14, 0));
        LocalTime fimTarde = safeTime(cfg.getFimTarde(), LocalTime.of(19, 0));

        boolean dentroManha = (hora.equals(iniManha) || hora.isAfter(iniManha)) && hora.isBefore(fimManha);
        boolean dentroTarde = (hora.equals(iniTarde) || hora.isAfter(iniTarde)) && hora.isBefore(fimTarde);

        if (!dentroManha && !dentroTarde) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Fora do horário de atendimento.");
        }

        // 7) respeitar o intervalo (30 em 30, etc.)
        LocalTime base = dentroManha ? iniManha : iniTarde;
        long minutos = Duration.between(base, hora).toMinutes();

        if (minutos < 0 || (minutos % intervalo != 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Horário inválido para o intervalo configurado.");
        }
    }

    private LocalTime safeTime(LocalTime t, LocalTime fallback) {
        return (t != null) ? t : fallback;
    }

    private boolean diaAtivo(FuncionamentoConfig cfg, DayOfWeek dow) {
        return switch (dow) {
            case MONDAY -> cfg.isSeg();
            case TUESDAY -> cfg.isTer();
            case WEDNESDAY -> cfg.isQua();
            case THURSDAY -> cfg.isQui();
            case FRIDAY -> cfg.isSex();
            case SATURDAY -> cfg.isSab();
            case SUNDAY -> cfg.isDom();
        };
    }
}
