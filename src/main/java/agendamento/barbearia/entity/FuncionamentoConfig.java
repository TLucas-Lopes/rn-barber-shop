package agendamento.barbearia.entity;

import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "funcionamento_config")
public class FuncionamentoConfig {

    @Id
    private Long id;

    @Column(nullable = false)
    private int intervaloMin = 30;

    @Column(nullable = false)
    private boolean seg = true;

    @Column(nullable = false)
    private boolean ter = true;

    @Column(nullable = false)
    private boolean qua = true;

    @Column(nullable = false)
    private boolean qui = true;

    @Column(nullable = false)
    private boolean sex = true;

    @Column(nullable = false)
    private boolean sab = true;

    @Column(nullable = false)
    private boolean dom = false;

    @Column(nullable = false)
    private LocalTime iniManha = LocalTime.of(8, 0);

    @Column(nullable = false)
    private LocalTime fimManha = LocalTime.of(12, 0);

    @Column(nullable = false)
    private LocalTime iniTarde = LocalTime.of(14, 0);

    @Column(nullable = false)
    private LocalTime fimTarde = LocalTime.of(19, 0);

    // GETTERS/SETTERS

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public int getIntervaloMin() { return intervaloMin; }
    public void setIntervaloMin(int intervaloMin) { this.intervaloMin = intervaloMin; }

    public boolean isSeg() { return seg; }
    public void setSeg(boolean seg) { this.seg = seg; }

    public boolean isTer() { return ter; }
    public void setTer(boolean ter) { this.ter = ter; }

    public boolean isQua() { return qua; }
    public void setQua(boolean qua) { this.qua = qua; }

    public boolean isQui() { return qui; }
    public void setQui(boolean qui) { this.qui = qui; }

    public boolean isSex() { return sex; }
    public void setSex(boolean sex) { this.sex = sex; }

    public boolean isSab() { return sab; }
    public void setSab(boolean sab) { this.sab = sab; }

    public boolean isDom() { return dom; }
    public void setDom(boolean dom) { this.dom = dom; }

    public LocalTime getIniManha() { return iniManha; }
    public void setIniManha(LocalTime iniManha) { this.iniManha = iniManha; }

    public LocalTime getFimManha() { return fimManha; }
    public void setFimManha(LocalTime fimManha) { this.fimManha = fimManha; }

    public LocalTime getIniTarde() { return iniTarde; }
    public void setIniTarde(LocalTime iniTarde) { this.iniTarde = iniTarde; }

    public LocalTime getFimTarde() { return fimTarde; }
    public void setFimTarde(LocalTime fimTarde) { this.fimTarde = fimTarde; }

    public static FuncionamentoConfig padrao() {
        FuncionamentoConfig c = new FuncionamentoConfig();
        c.setId(1L);
        c.setIntervaloMin(30);
        c.setSeg(true);
        c.setTer(true);
        c.setQua(true);
        c.setQui(true);
        c.setSex(true);
        c.setSab(true);
        c.setDom(false);

        c.setIniManha(LocalTime.of(8, 0));
        c.setFimManha(LocalTime.of(12, 0));
        c.setIniTarde(LocalTime.of(14, 0));
        c.setFimTarde(LocalTime.of(19, 0));
        return c;
    }
}
