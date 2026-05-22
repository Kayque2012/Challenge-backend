package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Atendimento {

    private int id;
    private LocalDateTime dataInicio;
    private String procedimentoRealizado;
    private String diagnosticoFinal;
    private String observacaoClinica;
    private String statusAtendimento;
    private String encaminhamento;
    private int idMatch;
    private int idDentista;

    public Atendimento() {}

    public Atendimento(int id, LocalDateTime dataInicio, String procedimentoRealizado,
                       String diagnosticoFinal, String observacaoClinica,
                       String statusAtendimento, String encaminhamento,
                       int idMatch, int idDentista) {
        this.id = id;
        this.dataInicio = dataInicio;
        this.procedimentoRealizado = procedimentoRealizado;
        this.diagnosticoFinal = diagnosticoFinal;
        this.observacaoClinica = observacaoClinica;
        this.statusAtendimento = statusAtendimento;
        this.encaminhamento = encaminhamento;
        this.idMatch = idMatch;
        this.idDentista = idDentista;
    }

    /** Conclui o atendimento, alterando o status para CONCLUIDO. */
    public void concluir() {
        this.statusAtendimento = "CONCLUIDO";
    }

    /** Cancela o atendimento, alterando o status para CANCELADO. */
    public void cancelar() {
        this.statusAtendimento = "CANCELADO";
    }

    /** Verifica se o atendimento ainda está em andamento. */
    public boolean isEmAndamento() {
        return "EM_ANDAMENTO".equals(statusAtendimento);
    }

    /** Registra o diagnóstico final do atendimento. */
    public void registrarDiagnostico(String diagnostico) {
        this.diagnosticoFinal = diagnostico;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDataInicio() { return dataInicio; }
    public void setDataInicio(LocalDateTime dataInicio) { this.dataInicio = dataInicio; }
    public String getProcedimentoRealizado() { return procedimentoRealizado; }
    public void setProcedimentoRealizado(String procedimentoRealizado) { this.procedimentoRealizado = procedimentoRealizado; }
    public String getDiagnosticoFinal() { return diagnosticoFinal; }
    public void setDiagnosticoFinal(String diagnosticoFinal) { this.diagnosticoFinal = diagnosticoFinal; }
    public String getObservacaoClinica() { return observacaoClinica; }
    public void setObservacaoClinica(String observacaoClinica) { this.observacaoClinica = observacaoClinica; }
    public String getStatusAtendimento() { return statusAtendimento; }
    public void setStatusAtendimento(String statusAtendimento) { this.statusAtendimento = statusAtendimento; }
    public String getEncaminhamento() { return encaminhamento; }
    public void setEncaminhamento(String encaminhamento) { this.encaminhamento = encaminhamento; }
    public int getIdMatch() { return idMatch; }
    public void setIdMatch(int idMatch) { this.idMatch = idMatch; }
    public int getIdDentista() { return idDentista; }
    public void setIdDentista(int idDentista) { this.idDentista = idDentista; }

    @Override
    public String toString() {
        return String.format("Atendimento{id=%d, status='%s', dentista=%d}", id, statusAtendimento, idDentista);
    }
}
