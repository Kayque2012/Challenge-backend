package br.com.fiap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Payload enviado pelo frontend para disparar um e-mail de lembrete/confirmação.
 *
 * tipo:
 *   "confirmacao"  — e-mail imediato ao confirmar a consulta
 *   "lembrete"     — lembrete periódico (D-3, D-2, D-1, D-0)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailLembreteDTO {

    public String tipo;          // "confirmacao" | "lembrete"
    public String para;          // e-mail do paciente
    public String nome;          // nome do paciente
    public String procedimento;
    public String data;          // "DD/MM/YYYY"
    public String hora;
    public String dentista;
    public int    diasAntes;     // 3, 2, 1 ou 0  (usado só em "lembrete")
}
