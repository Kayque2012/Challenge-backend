package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Paciente {

    private int id;

    @JsonProperty("nome")
    private String nomePaciente;

    private String email;
    private String senha;
    private String cpf;

    @JsonProperty("tipo")
    private String tipoPerfil;

    private LocalDate dataNascimento;
    private String genero;

    @JsonProperty("pais")
    private String pais;

    @JsonProperty("cidade")
    private String cidade;

    @JsonProperty("estado")
    private String estado;

    /**
     * Campo transiente: recebido do frontend na triagem/cadastro.
     * Não é persistido — é convertido para DATA_NASCIMENTO no PacienteBO antes do INSERT/UPDATE.
     */
    private int idade;
    private double rendaSalarioMinimo;

    /**
     * Coluna DESCRICAO_PROBLEMA do banco — usada como campo multi-propósito com prefixos:
     *   "ADOTADO:{idDentista}|TEL:{telefone}|{descricao}"
     *   "TEL:{telefone}|{descricao}"
     *   "{descricao}"
     * O parser fica em PacienteDAO.mapear(); a composição fica em PacienteBO.atualizar().
     */
    private String descricaoProblema;

    @JsonProperty("telefone")
    private String telefone;

    /**
     * Status do paciente — campo livre persistido no banco.
     * getStatus() é computado: retorna "adotado" se idDentistaAdotante != null.
     */
    @JsonProperty("status")
    private String status;

    /**
     * ID do dentista que adotou este paciente. Coluna própria após V2__normalize_paciente.sql.
     * Exposto também como idDentistaResponsavel via getter/setter para retrocompatibilidade com o frontend.
     */
    @JsonProperty("idDentistaAdotante")
    private Integer idDentistaAdotante;

    /** Mantido para não remover campo existente — getter/setter delegam a idDentistaAdotante. */
    @JsonIgnore
    private int idDentistaResponsavel;

    private String tipoDor;
    private int tempoDorDias;
    private String urgencia;
    private LocalDateTime criadoEm;
    private double latitude;
    private double longitude;

    public Paciente() {}

    /** Calcula idade aproximada pelo ano — suficiente para as regras de elegibilidade do projeto. */
    public int calcularIdade() {
        if (dataNascimento == null) return 0;
        return LocalDate.now().getYear() - dataNascimento.getYear();
    }

    /**
     * Elegibilidade do programa: jovem de 11–17 anos com renda familiar até 3 salários mínimos.
     * Usado em GET /pacientes/{id}/elegibilidade e no filtro de relatórios.
     */
    public boolean verificarElegibilidade() {
        return calcularIdade() >= 11 && calcularIdade() <= 17 && rendaSalarioMinimo <= 3.0;
    }

    /**
     * Classifica urgência com base no tipo de dor e tempo de dor declarados na triagem.
     *   ALTA  → dente quebrado, dor forte ou dor há mais de 7 dias
     *   MEDIA → dor moderada
     *   BAIXA → demais casos (incluindo paciente sem triagem)
     * Resultado persiste em URGENCIA e obedece ao CHECK do banco: ALTA/MEDIA/BAIXA.
     */
    public String calcularUrgencia() {
        if (tipoDor == null) return "BAIXA";
        if (tipoDor.equalsIgnoreCase("dente quebrado") || tipoDor.equalsIgnoreCase("forte") || tempoDorDias > 7) {
            return "ALTA";
        } else if (tipoDor.equalsIgnoreCase("moderada")) {
            return "MEDIA";
        }
        return "BAIXA";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNomePaciente() { return nomePaciente; }
    public void setNomePaciente(String nomePaciente) { this.nomePaciente = nomePaciente; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    @JsonIgnore
    public String getSenha() { return senha; }
    @JsonProperty("senha")
    public void setSenha(String senha) { this.senha = senha; }
    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public String getTipoPerfil() { return tipoPerfil; }
    public void setTipoPerfil(String tipoPerfil) { this.tipoPerfil = tipoPerfil; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }
    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public int getIdade() { return idade; }
    public void setIdade(int idade) { this.idade = idade; }
    public double getRendaSalarioMinimo() { return rendaSalarioMinimo; }
    public void setRendaSalarioMinimo(double rendaSalarioMinimo) { this.rendaSalarioMinimo = rendaSalarioMinimo; }
    public String getDescricaoProblema() { return descricaoProblema; }
    public void setDescricaoProblema(String descricaoProblema) { this.descricaoProblema = descricaoProblema; }
    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public String getStatus() { return idDentistaAdotante != null ? "adotado" : status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getIdDentistaAdotante() { return idDentistaAdotante; }
    public void setIdDentistaAdotante(Integer idDentistaAdotante) { this.idDentistaAdotante = idDentistaAdotante; }
    @JsonProperty("idDentistaResponsavel")
    public int getIdDentistaResponsavel() { return idDentistaAdotante != null ? idDentistaAdotante : 0; }
    @JsonProperty("idDentistaResponsavel")
    public void setIdDentistaResponsavel(int idDentistaResponsavel) { this.idDentistaAdotante = idDentistaResponsavel != 0 ? idDentistaResponsavel : null; }
    public String getTipoDor() { return tipoDor; }
    public void setTipoDor(String tipoDor) { this.tipoDor = tipoDor; }
    public int getTempoDorDias() { return tempoDorDias; }
    public void setTempoDorDias(int tempoDorDias) { this.tempoDorDias = tempoDorDias; }
    public String getUrgencia() { return urgencia; }
    public void setUrgencia(String urgencia) { this.urgencia = urgencia; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}
