package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {

    private int id;
    private LocalDateTime dataSolicitacao;
    private LocalDateTime dataAgendada;
    private String status;
    private double scoreMatch;
    private double prioridade;
    private String observacao;
    private LocalDateTime criadoEm;
    private Integer idAvaliacao;
    private int idPaciente;
    private int idEspecialidade;
    private int idDentista;

    public Match() {}

    public Match(int id, LocalDateTime dataSolicitacao, LocalDateTime dataAgendada,
                 String status, double scoreMatch, double prioridade, String observacao,
                 LocalDateTime criadoEm, Integer idAvaliacao, int idPaciente,
                 int idEspecialidade, int idDentista) {
        this.id = id;
        this.dataSolicitacao = dataSolicitacao;
        this.dataAgendada = dataAgendada;
        this.status = status;
        this.scoreMatch = scoreMatch;
        this.prioridade = prioridade;
        this.observacao = observacao;
        this.criadoEm = criadoEm;
        this.idAvaliacao = idAvaliacao;
        this.idPaciente = idPaciente;
        this.idEspecialidade = idEspecialidade;
        this.idDentista = idDentista;
    }

    /** Confirma o match, alterando o status para CONFIRMADO. */
    public void confirmar() {
        this.status = "CONFIRMADO";
    }

    /** Cancela o match, alterando o status para CANCELADO. */
    public void cancelar() {
        this.status = "CANCELADO";
    }

    /** Conclui o match, alterando o status para CONCLUIDO. */
    public void concluir() {
        this.status = "CONCLUIDO";
    }

    /** Verifica se o match ainda está ativo (PENDENTE ou CONFIRMADO). */
    public boolean isAtivo() {
        return "PENDENTE".equals(status) || "CONFIRMADO".equals(status);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDateTime dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }
    public LocalDateTime getDataAgendada() { return dataAgendada; }
    public void setDataAgendada(LocalDateTime dataAgendada) { this.dataAgendada = dataAgendada; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getScoreMatch() { return scoreMatch; }
    public void setScoreMatch(double scoreMatch) { this.scoreMatch = scoreMatch; }
    public double getPrioridade() { return prioridade; }
    public void setPrioridade(double prioridade) { this.prioridade = prioridade; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public Integer getIdAvaliacao() { return idAvaliacao; }
    public void setIdAvaliacao(Integer idAvaliacao) { this.idAvaliacao = idAvaliacao; }
    public int getIdPaciente() { return idPaciente; }
    public void setIdPaciente(int idPaciente) { this.idPaciente = idPaciente; }
    public int getIdEspecialidade() { return idEspecialidade; }
    public void setIdEspecialidade(int idEspecialidade) { this.idEspecialidade = idEspecialidade; }
    public int getIdDentista() { return idDentista; }
    public void setIdDentista(int idDentista) { this.idDentista = idDentista; }

    @Override
    public String toString() {
        return String.format("Match{id=%d, status='%s', paciente=%d, dentista=%d}", id, status, idPaciente, idDentista);
    }
}
