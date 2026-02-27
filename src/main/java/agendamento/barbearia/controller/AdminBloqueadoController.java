package agendamento.barbearia.controller;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import agendamento.barbearia.entity.DiaBloqueado;
import agendamento.barbearia.repository.DiaBloqueadoRepository;

@RestController
@RequestMapping("/admin/bloqueios")
@CrossOrigin
public class AdminBloqueadoController {

    private final DiaBloqueadoRepository repo;

    public AdminBloqueadoController(DiaBloqueadoRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<DiaBloqueado> listar() {
        List<DiaBloqueado> todos = repo.findAll();
        todos.sort(Comparator.comparing(DiaBloqueado::getData));
        return todos;
    }

    // POST /admin/bloqueios?data=YYYY-MM-DD
    @PostMapping
    public ResponseEntity<?> bloquear(@RequestParam String data) {
        LocalDate dia;
        try {
            dia = LocalDate.parse(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Data inválida.");
        }

        if (repo.existsByData(dia)) {
            return ResponseEntity.ok("Já bloqueado.");
        }

        repo.save(new DiaBloqueado(dia));
        return ResponseEntity.ok("Bloqueado.");
    }

    // ✅ DELETE POR ID: /admin/bloqueios/2  (isso mata seu 500 do front)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> desbloquearPorId(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }

    // DELETE POR DATA: /admin/bloqueios?data=YYYY-MM-DD
    @DeleteMapping
    public ResponseEntity<?> desbloquearPorData(@RequestParam String data) {
        LocalDate dia;
        try {
            dia = LocalDate.parse(data);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Data inválida.");
        }

        if (!repo.existsByData(dia)) {
            return ResponseEntity.notFound().build();
        }

        repo.deleteByData(dia);
        return ResponseEntity.noContent().build(); // 204
    }
}