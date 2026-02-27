package agendamento.barbearia.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import agendamento.barbearia.dto.TrocarSenhaDTO;
import agendamento.barbearia.security.AdminAccountService;
import jakarta.validation.Valid;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/admin/usuarios")
public class AdminAccountController {

    private final AdminAccountService service;

    public AdminAccountController(AdminAccountService service) {
        this.service = service;
    }

    @PostMapping("/trocar-senha")
    public ResponseEntity<Void> trocarSenha(@Valid @RequestBody TrocarSenhaDTO dto, Authentication auth) {
        service.trocarSenha(auth, dto);
        return ResponseEntity.noContent().build(); // 204
    }
}

