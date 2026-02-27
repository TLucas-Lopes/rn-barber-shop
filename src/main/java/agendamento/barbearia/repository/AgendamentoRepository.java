package agendamento.barbearia.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import agendamento.barbearia.dto.DiaSemanaDTO;
import agendamento.barbearia.dto.ResumoMensalDTO;
import agendamento.barbearia.entity.Agendamento;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    // Verifica se horário já está ocupado
    boolean existsByDataHora(LocalDateTime dataHora);

    // Lista por período (agenda do dia)
    List<Agendamento> findByDataHoraBetweenOrderByDataHoraAsc(
            LocalDateTime ini,
            LocalDateTime fim
    );

    // =========================
    // 📊 RELATÓRIO MENSAL
    // =========================
    @Query("""
      select new agendamento.barbearia.dto.ResumoMensalDTO(
        month(a.dataHora),
        count(a.id),
        coalesce(sum(a.valor), 0)
      )
      from Agendamento a
      where a.dataHora >= :ini and a.dataHora < :fim
      group by month(a.dataHora)
      order by month(a.dataHora)
    """)
    List<ResumoMensalDTO> resumoMensal(
            @Param("ini") LocalDateTime ini,
            @Param("fim") LocalDateTime fim
    );

    // =========================
    // 📊 RELATÓRIO SEMANAL (7 dias)
    // =========================
    @Query(value = """
        select
          cast(a.data_hora as date) as data,
          count(a.id) as atendimentos,
          coalesce(sum(a.valor), 0) as faturamento
        from agendamentos a
        where a.data_hora >= :ini and a.data_hora < :fim
        group by cast(a.data_hora as date)
        order by data
        """, nativeQuery = true)
    List<DiaSemanaDTO> resumoSemanal(
            @Param("ini") LocalDateTime ini,
            @Param("fim") LocalDateTime fim
    );
}





