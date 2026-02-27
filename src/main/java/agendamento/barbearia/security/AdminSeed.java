package agendamento.barbearia.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import org.springframework.security.crypto.password.PasswordEncoder;

import agendamento.barbearia.entity.AdminUser;
import agendamento.barbearia.repository.AdminUserRepository;

@Component
public class AdminSeed implements CommandLineRunner {

    private final AdminUserRepository repo;
    private final PasswordEncoder encoder;

    public AdminSeed(AdminUserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        // cria um admin padrão só se não existir
        if (!repo.existsByUsername("admin")) {
            String hash = encoder.encode("admin123");
            repo.save(new AdminUser("admin", hash, true));
            System.out.println("✅ Admin criado: admin / admin123");
        }
    }
}

