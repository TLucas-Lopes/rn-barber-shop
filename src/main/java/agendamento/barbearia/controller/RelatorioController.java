package agendamento.barbearia.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.*;

import agendamento.barbearia.dto.ResumoMensalDTO;
import agendamento.barbearia.repository.AgendamentoRepository;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/relatorios")
public class RelatorioController {

    private final AgendamentoRepository agRepo;

    public RelatorioController(AgendamentoRepository agRepo) {
        this.agRepo = agRepo;
    }

    @GetMapping("/mensal")
    public List<ResumoMensalDTO> mensal(@RequestParam int ano) {
        LocalDateTime ini = LocalDate.of(ano, 1, 1).atStartOfDay();
        LocalDateTime fim = LocalDate.of(ano + 1, 1, 1).atStartOfDay();
        return agRepo.resumoMensal(ini, fim);
    }
}

