package agendamento.barbearia.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import agendamento.barbearia.entity.DiaBloqueado;

public interface DiaBloqueadoRepository extends JpaRepository<DiaBloqueado, Long> {

    boolean existsByData(LocalDate data);

    Optional<DiaBloqueado> findByData(LocalDate data);

    void deleteByData(LocalDate data);
}
