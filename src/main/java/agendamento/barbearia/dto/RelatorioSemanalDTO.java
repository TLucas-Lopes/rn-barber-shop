package agendamento.barbearia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class RelatorioSemanalDTO {

    private LocalDate semanaInicio;
    private LocalDate semanaFim;

    private int totalAtendimentos;
    private BigDecimal totalFaturamento;

    private List<DiaSemanaDTO> dias;

    public RelatorioSemanalDTO() {}

    public RelatorioSemanalDTO(LocalDate semanaInicio, LocalDate semanaFim, int totalAtendimentos,
                               BigDecimal totalFaturamento, List<DiaSemanaDTO> dias) {
        this.semanaInicio = semanaInicio;
        this.semanaFim = semanaFim;
        this.totalAtendimentos = totalAtendimentos;
        this.totalFaturamento = totalFaturamento;
        this.dias = dias;
    }

    public LocalDate getSemanaInicio() { return semanaInicio; }
    public void setSemanaInicio(LocalDate semanaInicio) { this.semanaInicio = semanaInicio; }

    public LocalDate getSemanaFim() { return semanaFim; }
    public void setSemanaFim(LocalDate semanaFim) { this.semanaFim = semanaFim; }

    public int getTotalAtendimentos() { return totalAtendimentos; }
    public void setTotalAtendimentos(int totalAtendimentos) { this.totalAtendimentos = totalAtendimentos; }

    public BigDecimal getTotalFaturamento() { return totalFaturamento; }
    public void setTotalFaturamento(BigDecimal totalFaturamento) { this.totalFaturamento = totalFaturamento; }

    public List<DiaSemanaDTO> getDias() { return dias; }
    public void setDias(List<DiaSemanaDTO> dias) { this.dias = dias; }
}

