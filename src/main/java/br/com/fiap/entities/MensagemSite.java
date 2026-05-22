package br.com.fiap.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

/**
 * Mensagem enviada pelo formulario "Fale Conosco" do site.
 * Mapeada para a tabela T_SN_MENSAGEM_SITE.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MensagemSite {

    private int id;
    private String nome;
    private String email;
    private String assunto;
    private String mensagem;
    private LocalDateTime dataEnvio;

    public MensagemSite() {}

    public MensagemSite(int id, String nome, String email, String assunto,
                        String mensagem, LocalDateTime dataEnvio) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.assunto = assunto;
        this.mensagem = mensagem;
        this.dataEnvio = dataEnvio;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAssunto() { return assunto; }
    public void setAssunto(String assunto) { this.assunto = assunto; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    @Override
    public String toString() {
        return String.format("MensagemSite{id=%d, nome='%s', assunto='%s'}",
                id, nome, assunto);
    }
}
