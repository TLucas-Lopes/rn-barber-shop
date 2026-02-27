package agendamento.barbearia.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import agendamento.barbearia.entity.Servico;
import agendamento.barbearia.repository.ServicoRepository;

@RestController
@RequestMapping("/admin/servicos")
@CrossOrigin
public class AdminServicosController {

    private final ServicoRepository repo;

    public AdminServicosController(ServicoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Servico> listar() {
        return repo.findAll();
    }

    @PostMapping
    public ResponseEntity<?> criar(@RequestBody Servico s) {

        if (s.getNome() == null || s.getNome().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nome obrigatório");
        }

        if (s.getValor() == null) {
            return ResponseEntity.badRequest().body("Valor obrigatório");
        }

        return ResponseEntity.ok(repo.save(s));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id,
                                       @RequestBody Servico dados) {

        Servico s = repo.findById(id).orElse(null);

        if (s == null) return ResponseEntity.notFound().build();

        if (dados.getNome() != null)
            s.setNome(dados.getNome());

        if (dados.getValor() != null)
            s.setValor(dados.getValor());

        s.setAtivo(dados.isAtivo());

        return ResponseEntity.ok(repo.save(s));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id) {

        if (!repo.existsById(id))
            return ResponseEntity.notFound().build();

        repo.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
