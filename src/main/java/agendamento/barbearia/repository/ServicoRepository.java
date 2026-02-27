package agendamento.barbearia.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import agendamento.barbearia.entity.Servico;

public interface ServicoRepository extends JpaRepository<Servico, Long> {

    // para o cliente ver só os serviços ativos, ordenado pelo nome
    List<Servico> findByAtivoTrueOrderByNomeAsc();
}
