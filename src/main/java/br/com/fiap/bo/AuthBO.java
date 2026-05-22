package br.com.fiap.bo;

import br.com.fiap.dao.DentistaDAO;
import br.com.fiap.dao.PacienteDAO;
import br.com.fiap.entities.Dentista;
import br.com.fiap.entities.Paciente;
import br.com.fiap.exception.DatabaseException;
import br.com.fiap.util.SenhaUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;

/**
 * Camada de regras de negocio da autenticacao.
 *
 * Encapsula a logica de "qual tabela buscar primeiro" e "email ja existe?",
 * deixando o AuthResource limpo (so traduz HTTP <-> objetos).
 */
@ApplicationScoped
public class AuthBO {

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DentistaDAO dentistaDAO = new DentistaDAO();

    /**
     * Autentica email/senha verificando primeiro dentistas, depois pacientes.
     *
     * Estratégia dual-mode para migração transparente:
     *   1. Busca o usuário pelo e-mail (sem checar senha no SQL)
     *   2. Verifica a senha em Java com SenhaUtil.verificar()
     *      - Se o hash começa com "$2" → BCrypt (usuário já migrado)
     *      - Caso contrário → texto plano (usuário antigo) + migra automaticamente
     *   3. Retorna o objeto autenticado ou null se não encontrar / senha errada
     */
    public Object autenticar(String email, String senha) {
        try {
            // --- Tenta como dentista ---
            Dentista dentista = dentistaDAO.buscarPorEmail(email);
            if (dentista != null && SenhaUtil.verificar(senha, dentista.getSenha())) {
                migrarSenhaDentistaSePreciso(dentista, senha);
                return dentista;
            }

            // --- Tenta como paciente ---
            Paciente paciente = pacienteDAO.buscarPorEmail(email);
            if (paciente != null && SenhaUtil.verificar(senha, paciente.getSenha())) {
                migrarSenhaPacienteSePreciso(paciente, senha);
                return paciente;
            }

            return null;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao autenticar usuario.", e);
        }
    }

    /**
     * Se a senha ainda é texto plano, converte para BCrypt silenciosamente no próximo login.
     * A falha na migração não impede o acesso — o usuário continua logado.
     */
    private void migrarSenhaDentistaSePreciso(Dentista d, String senhaPlana) {
        if (!SenhaUtil.eHashBcrypt(d.getSenha())) {
            try {
                dentistaDAO.atualizarSenha(d.getId(), SenhaUtil.hashear(senhaPlana));
            } catch (SQLException e) {
                System.err.println("[Auth] Falha ao migrar senha do dentista id=" + d.getId() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Se a senha ainda é texto plano, converte para BCrypt silenciosamente no próximo login.
     * A falha na migração não impede o acesso — o usuário continua logado.
     */
    private void migrarSenhaPacienteSePreciso(Paciente p, String senhaPlana) {
        if (!SenhaUtil.eHashBcrypt(p.getSenha())) {
            try {
                pacienteDAO.atualizarSenha(p.getId(), SenhaUtil.hashear(senhaPlana));
            } catch (SQLException e) {
                System.err.println("[Auth] Falha ao migrar senha do paciente id=" + p.getId() + ": " + e.getMessage());
            }
        }
    }

    /** Verifica se um email ja esta cadastrado em qualquer das duas tabelas. */
    public boolean emailJaCadastrado(String email) {
        try {
            return pacienteDAO.existeEmail(email) || dentistaDAO.existeEmail(email);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao verificar email.", e);
        }
    }

    /** Cadastra um paciente novo. */
    public Paciente cadastrarPaciente(Paciente paciente) {
        try {
            pacienteDAO.inserir(paciente);
            return paciente;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao cadastrar paciente.", e);
        }
    }

    /** Cadastra um dentista novo. */
    public Dentista cadastrarDentista(Dentista dentista) {
        try {
            dentistaDAO.inserir(dentista);
            return dentista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao cadastrar dentista.", e);
        }
    }
}
