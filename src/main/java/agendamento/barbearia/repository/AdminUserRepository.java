package agendamento.barbearia.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import agendamento.barbearia.entity.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {
    Optional<AdminUser> findByUsername(String username);
    boolean existsByUsername(String username);
}

