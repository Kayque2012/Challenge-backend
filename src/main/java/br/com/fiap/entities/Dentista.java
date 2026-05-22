package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dentista {

    private int id;

    @JsonProperty("nome")
    private String nomeDentista;

    private String email;
    private String senha;
    private String cro;

    @JsonProperty("tipo")
    private String tipoPerfil;

    private int maxPacientesMes;
    private int atendidosMes;

    @JsonProperty("pais")
    private String pais;

    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("estado")
    private String estado;

    private String descricaoClinica;
    private LocalDateTime criadoEm;
    private double latitude;
    private double longitude;

    public Dentista() {}

    public boolean temVagaDisponivel() {
        return this.atendidosMes < this.maxPacientesMes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomeDentista() { return nomeDentista; }
    public void setNomeDentista(String nomeDentista) { this.nomeDentista = nomeDentista; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonIgnore
    public String getSenha() { return senha; }
    @JsonProperty("senha")
    public void setSenha(String senha) { this.senha = senha; }
    public String getCro() { return cro; }
    public void setCro(String cro) { this.cro = cro; }
    public String getTipoPerfil() { return tipoPerfil; }
    public void setTipoPerfil(String tipoPerfil) { this.tipoPerfil = tipoPerfil; }
    public int getMaxPacientesMes() { return maxPacientesMes; }
    public void setMaxPacientesMes(int maxPacientesMes) { this.maxPacientesMes = maxPacientesMes; }
    public int getAtendidosMes() { return atendidosMes; }
    public void setAtendidosMes(int atendidosMes) { this.atendidosMes = atendidosMes; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getDescricaoClinica() { return descricaoClinica; }
    public void setDescricaoClinica(String descricaoClinica) { this.descricaoClinica = descricaoClinica; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
