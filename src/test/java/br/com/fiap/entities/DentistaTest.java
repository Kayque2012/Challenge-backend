package br.com.fiap.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Dentista.
 * Sem @QuarkusTest — roda como JUnit puro, sem iniciar o servidor ou banco.
 */
@DisplayName("Dentista — lógica de negócio")
class DentistaTest {

    // ─── temVagaDisponivel ────────────────────────────────────────────────────

    @Test
    @DisplayName("tem vaga quando atendidos < máximo")
    void temVagaDisponivel_retornaTrue_quandoAbaixoDoLimite() {
        Dentista d = new Dentista();
        d.setMaxPacientesMes(10);
        d.setAtendidosMes(5);
        assertTrue(d.temVagaDisponivel());
    }

    @Test
    @DisplayName("sem vaga quando atendidos == máximo")
    void temVagaDisponivel_retornaFalse_quandoAtingeLimite() {
        Dentista d = new Dentista();
        d.setMaxPacientesMes(10);
        d.setAtendidosMes(10);
        assertFalse(d.temVagaDisponivel());
    }

    @Test
    @DisplayName("sem vaga quando atendidos > máximo (dado inconsistente)")
    void temVagaDisponivel_retornaFalse_quandoExcedeLimite() {
        Dentista d = new Dentista();
        d.setMaxPacientesMes(10);
        d.setAtendidosMes(15);
        assertFalse(d.temVagaDisponivel());
    }

    @Test
    @DisplayName("tem vaga no início do mês (atendidos = 0)")
    void temVagaDisponivel_retornaTrue_comAtendidosZerado() {
        Dentista d = new Dentista();
        d.setMaxPacientesMes(10);
        d.setAtendidosMes(0);
        assertTrue(d.temVagaDisponivel());
    }

    @Test
    @DisplayName("tem vaga com apenas uma consulta restante")
    void temVagaDisponivel_retornaTrue_comUmaVagaRestante() {
        Dentista d = new Dentista();
        d.setMaxPacientesMes(10);
        d.setAtendidosMes(9);
        assertTrue(d.temVagaDisponivel());
    }

    @Test
    @DisplayName("dentista com limite alto nunca fica sem vaga cedo")
    void temVagaDisponivel_comLimiteAlto_mantemVagas() {
        Dentista d = new Dentista();
        d.setMaxPacientesMes(100);
        d.setAtendidosMes(50);
        assertTrue(d.temVagaDisponivel());
    }
}
