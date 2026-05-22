package br.com.fiap.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para a entidade Paciente.
 * Sem @QuarkusTest — roda como JUnit puro, sem iniciar o servidor ou banco.
 */
@DisplayName("Paciente — lógica de negócio")
class PacienteTest {

    // ─── calcularIdade ────────────────────────────────────────────────────────

    @Test
    @DisplayName("calcularIdade retorna a idade correta baseada na data de nascimento")
    void calcularIdade_retornaIdadeCorreta() {
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(15));
        assertEquals(15, p.calcularIdade());
    }

    @Test
    @DisplayName("calcularIdade retorna 0 quando data de nascimento é nula")
    void calcularIdade_retornaZero_quandoDataNula() {
        Paciente p = new Paciente();
        assertEquals(0, p.calcularIdade());
    }

    // ─── verificarElegibilidade ───────────────────────────────────────────────

    @Test
    @DisplayName("elegível: jovem de 14 anos com renda de 1.5 SM")
    void verificarElegibilidade_retornaTrue_paraJovemComBaixaRenda() {
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(14));
        p.setRendaSalarioMinimo(1.5);
        assertTrue(p.verificarElegibilidade());
    }

    @Test
    @DisplayName("não elegível: adulto de 25 anos (fora da faixa etária)")
    void verificarElegibilidade_retornaFalse_paraAdulto() {
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(25));
        p.setRendaSalarioMinimo(1.0);
        assertFalse(p.verificarElegibilidade());
    }

    @Test
    @DisplayName("não elegível: jovem com renda acima de 3 SM")
    void verificarElegibilidade_retornaFalse_paraRendaAlta() {
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(15));
        p.setRendaSalarioMinimo(3.5);
        assertFalse(p.verificarElegibilidade());
    }

    @Test
    @DisplayName("não elegível: criança de 10 anos (abaixo do mínimo)")
    void verificarElegibilidade_retornaFalse_paraCriancaMaisNova() {
        Paciente p = new Paciente();
        p.setDataNascimento(LocalDate.now().minusYears(10));
        p.setRendaSalarioMinimo(1.0);
        assertFalse(p.verificarElegibilidade());
    }

    // ─── calcularUrgencia ─────────────────────────────────────────────────────

    @Test
    @DisplayName("urgência ALTA para dente quebrado")
    void calcularUrgencia_retornaAlta_paraDenteQuebrado() {
        Paciente p = new Paciente();
        p.setTipoDor("dente quebrado");
        p.setTempoDorDias(2);
        assertEquals("ALTA", p.calcularUrgencia());
    }

    @Test
    @DisplayName("urgência ALTA para dor forte")
    void calcularUrgencia_retornaAlta_paraDorForte() {
        Paciente p = new Paciente();
        p.setTipoDor("forte");
        p.setTempoDorDias(3);
        assertEquals("ALTA", p.calcularUrgencia());
    }

    @Test
    @DisplayName("urgência ALTA quando dor dura mais de 7 dias independente do tipo")
    void calcularUrgencia_retornaAlta_paraDorLonga() {
        Paciente p = new Paciente();
        p.setTipoDor("leve");
        p.setTempoDorDias(10);
        assertEquals("ALTA", p.calcularUrgencia());
    }

    @Test
    @DisplayName("urgência MEDIA para dor moderada de curta duração")
    void calcularUrgencia_retornaMedia_paraDorModerada() {
        Paciente p = new Paciente();
        p.setTipoDor("moderada");
        p.setTempoDorDias(3);
        assertEquals("MEDIA", p.calcularUrgencia());
    }

    @Test
    @DisplayName("urgência BAIXA para dor leve de curta duração")
    void calcularUrgencia_retornaBaixa_paraDorLeve() {
        Paciente p = new Paciente();
        p.setTipoDor("leve");
        p.setTempoDorDias(2);
        assertEquals("BAIXA", p.calcularUrgencia());
    }

    @Test
    @DisplayName("urgência BAIXA quando tipoDor é nulo")
    void calcularUrgencia_retornaBaixa_quandoTipoDorNulo() {
        Paciente p = new Paciente();
        assertNull(p.getTipoDor());
        assertEquals("BAIXA", p.calcularUrgencia());
    }
}
