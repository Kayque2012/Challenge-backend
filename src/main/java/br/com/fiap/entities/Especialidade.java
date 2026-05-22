package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Especialidade {

    private int id;
    private String nomeEspecialidade;

    public Especialidade() {}

    public Especialidade(int id, String nomeEspecialidade) {
        this.id = id;
        this.nomeEspecialidade = nomeEspecialidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeEspecialidade() { return nomeEspecialidade; }
    public void setNomeEspecialidade(String nomeEspecialidade) { this.nomeEspecialidade = nomeEspecialidade; }

    @Override
    public String toString() {
        return String.format("Especialidade{id=%d, nome='%s'}", id, nomeEspecialidade);
    }
}
