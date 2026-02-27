package agendamento.barbearia.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "funcionamento_config")
public class ConfigFuncionamento {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private Integer intervaloMin = 30;

    @Column(nullable = false) private boolean seg = true;
    @Column(nullable = false) private boolean ter = true;
    @Column(nullable = false) private boolean qua = true;
    @Column(nullable = false) private boolean qui = true;
    @Column(nullable = false) private boolean sex = true;
    @Column(nullable = false) private boolean sab = false;
    @Column(nullable = false) private boolean dom = false;

    @Column(nullable = false) private String iniManha = "08:00";
    @Column(nullable = false) private String fimManha = "12:00";
    @Column(nullable = false) private String iniTarde = "14:00";
    @Column(nullable = false) private String fimTarde = "19:00";

    public ConfigFuncionamento() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getIntervaloMin() { return intervaloMin; }
    public void setIntervaloMin(Integer intervaloMin) { this.intervaloMin = intervaloMin; }

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

    public String getIniManha() { return iniManha; }
    public void setIniManha(String iniManha) { this.iniManha = iniManha; }

    public String getFimManha() { return fimManha; }
    public void setFimManha(String fimManha) { this.fimManha = fimManha; }

    public String getIniTarde() { return iniTarde; }
    public void setIniTarde(String iniTarde) { this.iniTarde = iniTarde; }

    public String getFimTarde() { return fimTarde; }
    public void setFimTarde(String fimTarde) { this.fimTarde = fimTarde; }

   public static ConfigFuncionamento padrao() {
    ConfigFuncionamento c = new ConfigFuncionamento();

    c.setId(1L);

    c.setIntervaloMin(30);

    c.setSeg(true);
    c.setTer(true);
    c.setQua(true);
    c.setQui(true);
    c.setSex(true);
    c.setSab(true);
    c.setDom(false);

    c.setIniManha("08:00");
    c.setFimManha("12:00");

    c.setIniTarde("14:00");
    c.setFimTarde("19:00");

    return c;
}


    
}
