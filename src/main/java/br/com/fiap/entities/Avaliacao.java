package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Avaliacao {

    private int id;
    private double notaPacienteParaDentista;
    private String comentarioPaciente;
    private LocalDateTime criadoEm;

    public Avaliacao() {}

    public Avaliacao(int id, double notaPacienteParaDentista, String comentarioPaciente, LocalDateTime criadoEm) {
        this.id = id;
        this.notaPacienteParaDentista = notaPacienteParaDentista;
        this.comentarioPaciente = comentarioPaciente;
        this.criadoEm = criadoEm;
    }

    /** Verifica se a nota é válida (entre 0 e 10). */
    public boolean notaValida() {
        return notaPacienteParaDentista >= 0 && notaPacienteParaDentista <= 10;
    }

    /** Classifica a avaliação em categorias textuais. */
    public String classificar() {
        if (notaPacienteParaDentista >= 9) return "EXCELENTE";
        if (notaPacienteParaDentista >= 7) return "BOM";
        if (notaPacienteParaDentista >= 5) return "REGULAR";
        return "RUIM";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public double getNotaPacienteParaDentista() { return notaPacienteParaDentista; }
    public void setNotaPacienteParaDentista(double notaPacienteParaDentista) { this.notaPacienteParaDentista = notaPacienteParaDentista; }
    public String getComentarioPaciente() { return comentarioPaciente; }
    public void setComentarioPaciente(String comentarioPaciente) { this.comentarioPaciente = comentarioPaciente; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    @Override
    public String toString() {
        return String.format("Avaliacao{id=%d, nota=%.1f, classificacao='%s'}", id, notaPacienteParaDentista, classificar());
    }
}
