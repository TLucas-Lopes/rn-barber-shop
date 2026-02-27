package agendamento.barbearia.dto;

import java.math.BigDecimal;

public class ResumoMensalDTO {
    private int mes;              // 1..12
    private long qtd;             // quantidade de atendimentos
    private BigDecimal total;     // soma do valor

    public ResumoMensalDTO(int mes, long qtd, BigDecimal total) {
        this.mes = mes;
        this.qtd = qtd;
        this.total = total;
    }

    public int getMes() { return mes; }
    public long getQtd() { return qtd; }
    public BigDecimal getTotal() { return total; }
}

