package agendamento.barbearia.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import agendamento.barbearia.entity.Servico;
import agendamento.barbearia.repository.ServicoRepository;

@RestController
@RequestMapping("/servicos")
@CrossOrigin
public class ServicosController {

    private final ServicoRepository repository;

    public ServicosController(ServicoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Servico> listarAtivos() {
        return repository.findByAtivoTrueOrderByNomeAsc();
    }
}
