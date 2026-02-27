package agendamento.barbearia.security;

import agendamento.barbearia.dto.TrocarSenhaDTO;
import agendamento.barbearia.entity.AdminUser;
import agendamento.barbearia.repository.AdminUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.*;

@Service
public class AdminAccountService {

    private final AdminUserRepository repo;
    private final PasswordEncoder encoder;

    public AdminAccountService(AdminUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public void trocarSenha(Authentication auth, TrocarSenhaDTO dto) {
        if (auth == null || auth.getName() == null) {
            throw new ResponseStatusException(UNAUTHORIZED, "Não autenticado");
        }

        if (!dto.getNovaSenha().equals(dto.getConfirmarSenha())) {
            throw new ResponseStatusException(BAD_REQUEST, "Confirmação de senha não confere");
        }

        String username = auth.getName();

        AdminUser u = repo.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Usuário admin não encontrado"));

        // valida senha atual
        if (!encoder.matches(dto.getSenhaAtual(), u.getPasswordHash())) {
            throw new ResponseStatusException(BAD_REQUEST, "Senha atual incorreta");
        }

        // salva nova senha
        u.setPasswordHash(encoder.encode(dto.getNovaSenha()));
        repo.save(u);
    }
}

