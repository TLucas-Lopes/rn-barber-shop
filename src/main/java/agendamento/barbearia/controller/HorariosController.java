package agendamento.barbearia.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import agendamento.barbearia.entity.Agendamento;
import agendamento.barbearia.entity.FuncionamentoConfig;
import agendamento.barbearia.repository.AgendamentoRepository;
import agendamento.barbearia.repository.DiaBloqueadoRepository;
import agendamento.barbearia.repository.FuncionamentoConfigRepository;

@RestController
@RequestMapping("/horarios")
@CrossOrigin
public class HorariosController {

    private final FuncionamentoConfigRepository cfgRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final DiaBloqueadoRepository bloqueioRepository;

    public HorariosController(FuncionamentoConfigRepository cfgRepository,
                              AgendamentoRepository agendamentoRepository,
                              DiaBloqueadoRepository bloqueioRepository) {
        this.cfgRepository = cfgRepository;
        this.agendamentoRepository = agendamentoRepository;
        this.bloqueioRepository = bloqueioRepository;
    }

    @GetMapping("/disponiveis")
    public List<String> listarHorarios(@RequestParam String data) {

        LocalDate dia;
        try {
            dia = LocalDate.parse(data); // YYYY-MM-DD
        } catch (Exception e) {
            return List.of();
        }

        // ✅ Janela móvel de 7 dias (cliente só vê até 7 dias contando hoje)
        LocalDate hoje = LocalDate.now();
         LocalDate limite = hoje.plusDays(6);

       if (dia.isBefore(hoje) || dia.isAfter(limite)) {
        return List.of();
        }


        // ✅ Se o dia estiver bloqueado, NÃO retorna horários
        if (bloqueioRepository.existsByData(dia)) {
            return List.of();
        }

        FuncionamentoConfig cfg = cfgRepository.findById(1L).orElseGet(this::defaultCfg);

        // ✅ dia ativo?
        if (!diaAtivo(cfg, dia.getDayOfWeek())) {
            return List.of();
        }

        int intervalo = cfg.getIntervaloMin();
        if (intervalo <= 0) intervalo = 30;

        LocalTime iniManha = (cfg.getIniManha() != null) ? cfg.getIniManha() : LocalTime.of(8, 0);
        LocalTime fimManha = (cfg.getFimManha() != null) ? cfg.getFimManha() : LocalTime.of(12, 0);
        LocalTime iniTarde = (cfg.getIniTarde() != null) ? cfg.getIniTarde() : LocalTime.of(14, 0);
        LocalTime fimTarde = (cfg.getFimTarde() != null) ? cfg.getFimTarde() : LocalTime.of(19, 0);

        List<String> horarios = new ArrayList<>();
        addFaixa(horarios, iniManha, fimManha, intervalo);
        addFaixa(horarios, iniTarde, fimTarde, intervalo);

        // ocupados
        List<Agendamento> todos = agendamentoRepository.findAll();
        Set<String> ocupados = new HashSet<>();

        for (Agendamento ag : todos) {
            if (ag.getDataHora() == null) continue;
            if (ag.getDataHora().toLocalDate().equals(dia)) {
                ocupados.add(formatHora(ag.getDataHora().toLocalTime()));
            }
        }

        horarios.removeIf(ocupados::contains);

        // se for hoje, remove passados
        if (dia.equals(LocalDate.now())) {
            LocalTime agora = LocalTime.now();
            horarios.removeIf(h -> LocalTime.parse(h).isBefore(agora));
        }

        return horarios;
    }

    // -------- helpers --------

    private void addFaixa(List<String> out, LocalTime ini, LocalTime fim, int intervaloMin) {
        if (ini == null || fim == null) return;
        if (!ini.isBefore(fim)) return;

        LocalTime t = ini;
        while (t.isBefore(fim)) {
            out.add(formatHora(t));
            t = t.plusMinutes(intervaloMin);
        }
    }

    private String formatHora(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
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

    private FuncionamentoConfig defaultCfg() {
        FuncionamentoConfig c = new FuncionamentoConfig();
        try { c.setId(1L); } catch (Exception ignored) {}

        c.setIntervaloMin(30);

        c.setSeg(true);
        c.setTer(true);
        c.setQua(true);
        c.setQui(true);
        c.setSex(true);
        c.setSab(false);
        c.setDom(false);

        c.setIniManha(LocalTime.of(8, 0));
        c.setFimManha(LocalTime.of(12, 0));
        c.setIniTarde(LocalTime.of(14, 0));
        c.setFimTarde(LocalTime.of(19, 0));

        return c;
    }
}
