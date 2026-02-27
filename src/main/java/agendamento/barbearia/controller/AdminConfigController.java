package agendamento.barbearia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import agendamento.barbearia.entity.FuncionamentoConfig;
import agendamento.barbearia.repository.FuncionamentoConfigRepository;

@RestController
@RequestMapping("/admin/config")
@CrossOrigin
public class AdminConfigController {

    private final FuncionamentoConfigRepository repository;

    public AdminConfigController(FuncionamentoConfigRepository repository) {
        this.repository = repository;
    }

    // ✅ Sempre retorna uma config (se não existir, cria e salva a padrão)
    @GetMapping
    public FuncionamentoConfig getConfig() {
        return garantirConfig();
    }

    // ✅ Salva e devolve o que ficou no banco
    @PutMapping
    public ResponseEntity<FuncionamentoConfig> salvar(@RequestBody FuncionamentoConfig cfg) {

        // garante ID fixo 1 (pra não criar várias configs)
        cfg.setId(1L);

        // Ajustes de segurança
        if (cfg.getIntervaloMin() <= 0) cfg.setIntervaloMin(30);

        // Se algum horário vier null, mantém o que já existe no banco
        FuncionamentoConfig atual = garantirConfig();

        if (cfg.getIniManha() == null) cfg.setIniManha(atual.getIniManha());
        if (cfg.getFimManha() == null) cfg.setFimManha(atual.getFimManha());
        if (cfg.getIniTarde() == null) cfg.setIniTarde(atual.getIniTarde());
        if (cfg.getFimTarde() == null) cfg.setFimTarde(atual.getFimTarde());

        FuncionamentoConfig salvo = repository.save(cfg);
        return ResponseEntity.ok(salvo);
    }

    // ================= helpers =================

    private FuncionamentoConfig garantirConfig() {
        return repository.findById(1L).orElseGet(() -> repository.save(FuncionamentoConfig.padrao()));
    }
}
