package agendamento.barbearia.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import agendamento.barbearia.entity.AdminUser;
import agendamento.barbearia.repository.AdminUserRepository;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository repo;

    public AdminUserDetailsService(AdminUserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser u = repo.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return new User(
                u.getUsername(),
                u.getPasswordHash(),
                u.isEnabled(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
    }
}

