package agendamento.barbearia.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DiaSemanaDTO {

    private LocalDate data;
    private int atendimentos;
    private BigDecimal faturamento;

    public DiaSemanaDTO() {}

    public DiaSemanaDTO(LocalDate data, int atendimentos, BigDecimal faturamento) {
        this.data = data;
        this.atendimentos = atendimentos;
        this.faturamento = faturamento;
    }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public int getAtendimentos() { return atendimentos; }
    public void setAtendimentos(int atendimentos) { this.atendimentos = atendimentos; }

    public BigDecimal getFaturamento() { return faturamento; }
    public void setFaturamento(BigDecimal faturamento) { this.faturamento = faturamento; }
}

