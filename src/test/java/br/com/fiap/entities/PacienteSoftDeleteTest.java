package br.com.fiap.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a lógica de soft-delete/adoção introduzida na Fase 2.
 * Sem @QuarkusTest — roda como JUnit puro, sem iniciar o servidor ou banco.
 */
@DisplayName("Paciente — soft delete e adoção (Fase 2)")
class PacienteSoftDeleteTest {

    // ─── getStatus() computado ────────────────────────────────────────────────

    @Test
    @DisplayName("getStatus retorna 'adotado' quando idDentistaAdotante não é null")
    void getStatus_retornaAdotado_quandoIdDentistaAdotantePreenchido() {
        Paciente p = new Paciente();
        p.setIdDentistaAdotante(42);
        assertEquals("adotado", p.getStatus());
    }

    @Test
    @DisplayName("getStatus retorna null quando idDentistaAdotante é null e status não foi definido")
    void getStatus_retornaNull_quandoIdDentistaAdotanteNuloESemStatus() {
        Paciente p = new Paciente();
        assertNull(p.getStatus());
    }

    @Test
    @DisplayName("getStatus retorna o valor do campo status quando idDentistaAdotante é null")
    void getStatus_retornaStatusCampo_quandoIdDentistaAdotanteNulo() {
        Paciente p = new Paciente();
        p.setStatus("aguardando");
        assertEquals("aguardando", p.getStatus());
    }

    // ─── alias idDentistaResponsavel ──────────────────────────────────────────

    @Test
    @DisplayName("setIdDentistaResponsavel(0) deve setar idDentistaAdotante como null")
    void setIdDentistaResponsavel_zero_setaIdDentistaAdotanteComoNull() {
        Paciente p = new Paciente();
        p.setIdDentistaResponsavel(0);
        assertNull(p.getIdDentistaAdotante());
    }

    @Test
    @DisplayName("setIdDentistaResponsavel(5) deve setar idDentistaAdotante como 5")
    void setIdDentistaResponsavel_cinco_setaIdDentistaAdotanteComo5() {
        Paciente p = new Paciente();
        p.setIdDentistaResponsavel(5);
        assertEquals(5, p.getIdDentistaAdotante());
    }

    @Test
    @DisplayName("getIdDentistaResponsavel retorna 0 quando idDentistaAdotante é null")
    void getIdDentistaResponsavel_retornaZero_quandoIdDentistaAdotanteNulo() {
        Paciente p = new Paciente();
        assertEquals(0, p.getIdDentistaResponsavel());
    }

    @Test
    @DisplayName("getIdDentistaResponsavel retorna o id quando idDentistaAdotante está preenchido")
    void getIdDentistaResponsavel_retornaId_quandoIdDentistaAdotantePreenchido() {
        Paciente p = new Paciente();
        p.setIdDentistaAdotante(7);
        assertEquals(7, p.getIdDentistaResponsavel());
    }
}
