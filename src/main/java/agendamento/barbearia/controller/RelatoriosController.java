package agendamento.barbearia.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import agendamento.barbearia.dto.DiaSemanaDTO;
import agendamento.barbearia.dto.RelatorioSemanalDTO;
import agendamento.barbearia.entity.Agendamento;
import agendamento.barbearia.repository.AgendamentoRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/relatorios")
public class RelatoriosController {

    private final AgendamentoRepository agRepo;

    public RelatoriosController(AgendamentoRepository agRepo) {
        this.agRepo = agRepo;
    }

    /**
     * GET /admin/relatorios/semanal
     * Opcional: ?data=YYYY-MM-DD (qualquer dia dentro da semana que você quer)
     * Se não passar, usa hoje.
     */
    @GetMapping("/semanal")
    public RelatorioSemanalDTO semanal(@RequestParam(required = false) String data) {

        LocalDate base = (data == null || data.isBlank())
                ? LocalDate.now()
                : LocalDate.parse(data);

        // Semana começando na SEGUNDA (Brasil)
        WeekFields wf = WeekFields.of(new Locale("pt", "BR"));
        LocalDate inicio = base.with(wf.dayOfWeek(), 1); // segunda
        LocalDate fim = inicio.plusDays(6);              // domingo

        LocalDateTime iniDT = inicio.atStartOfDay();
        LocalDateTime fimDT = fim.plusDays(1).atStartOfDay(); // exclusivo

        List<Agendamento> ags = agRepo.findByDataHoraBetweenOrderByDataHoraAsc(iniDT, fimDT);

        // Preenche os 7 dias (mesmo se não tiver nada)
        List<DiaSemanaDTO> dias = new ArrayList<>();
        BigDecimal totalFat = BigDecimal.ZERO;

        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicio.plusDays(i);

            int atend = 0;
            BigDecimal fat = BigDecimal.ZERO;

            for (Agendamento a : ags) {
                LocalDate d = a.getDataHora().toLocalDate();
                if (d.equals(dia)) {
                    atend++;
                    if (a.getValor() != null) fat = fat.add(a.getValor());
                }
            }

            totalFat = totalFat.add(fat);
            dias.add(new DiaSemanaDTO(dia, atend, fat));
        }

        return new RelatorioSemanalDTO(
                inicio,
                fim,
                ags.size(),
                totalFat,
                dias
        );
    }
}

