package br.com.fiap.bo;

import br.com.fiap.dao.DentistaDAO;
import br.com.fiap.entities.Dentista;
import br.com.fiap.exception.DatabaseException;
import br.com.fiap.exception.ResourceNotFoundException;
import br.com.fiap.exception.ValidationException;
import br.com.fiap.util.GeocodificacaoUtil;
import br.com.fiap.util.SenhaUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class DentistaBO {

    private final DentistaDAO dao = new DentistaDAO();

    public Dentista cadastrar(Dentista dentista) {
        validar(dentista);

        try {
            if (dao.existeEmail(dentista.getEmail())) {
                throw new ValidationException("E-mail ja cadastrado. Use outro e-mail ou faca login.");
            }
        } catch (ValidationException ve) {
            throw ve;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao verificar e-mail.", e);
        }

        if (dentista.getSenha() != null && !dentista.getSenha().isBlank()
                && !SenhaUtil.eHashBcrypt(dentista.getSenha())) {
            dentista.setSenha(SenhaUtil.hashear(dentista.getSenha()));
        }

        if (dentista.getMaxPacientesMes() <= 0) dentista.setMaxPacientesMes(10);
        dentista.setAtendidosMes(0);
        try {
            dao.inserir(dentista);

            final int idGerado = dentista.getId();
            final String cidade = dentista.getCidade();
            final String estado = dentista.getEstado() != null ? dentista.getEstado() : "";
            final String pais = dentista.getPais() != null ? dentista.getPais() : "Brasil";
            new Thread(() -> {
                double[] coords = GeocodificacaoUtil.buscar(cidade, estado, pais);
                if (coords != null) {
                    try { dao.atualizarCoordenadas(idGerado, coords[0], coords[1]); }
                    catch (Exception ignored) {}
                }
            }).start();

            return dentista;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00001")) {
                throw new ValidationException("CRO ou e-mail ja cadastrado no sistema.");
            }
            throw new DatabaseException("Erro ao cadastrar dentista.", e);
        }
    }

    public List<Dentista> listar() {
        try {
            return dao.listarTodos();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar dentistas.", e);
        }
    }

    public Dentista buscarPorId(int id) {
        try {
            Dentista d = dao.buscarPorId(id);
            if (d == null) throw new ResourceNotFoundException("Dentista", id);
            return d;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar dentista.", e);
        }
    }

    public Dentista atualizar(int id, Dentista dentista) {
        try {
            Dentista existente = dao.buscarPorId(id);
            if (existente == null) throw new ResourceNotFoundException("Dentista", id);
            dentista.setId(id);
            dao.atualizar(dentista);
            return dentista;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar dentista.", e);
        }
    }

    public void excluir(int id) {
        try {
            if (dao.buscarPorId(id) == null) throw new ResourceNotFoundException("Dentista", id);
            dao.deletar(id);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao excluir dentista.", e);
        }
    }

    public List<Dentista> listarInativos() {
        try {
            return dao.listarInativos();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar dentistas inativos.", e);
        }
    }

    public void reativar(int id) {
        try {
            dao.reativar(id);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao reativar dentista.", e);
        }
    }

    public boolean temVagaDisponivel(int id) {
        return buscarPorId(id).temVagaDisponivel();
    }

    private void validar(Dentista d) {
        if (d == null) throw new ValidationException("Dentista nao pode ser nulo.");
        if (d.getNomeDentista() == null || d.getNomeDentista().isBlank())
            throw new ValidationException("Nome do dentista e obrigatorio.");
        if (d.getEmail() == null || d.getEmail().isBlank())
            throw new ValidationException("Email e obrigatorio.");
        if (d.getPais() == null || d.getPais().isBlank())
            throw new ValidationException("Pais e obrigatorio.");
        if (d.getCidade() == null || d.getCidade().isBlank())
            throw new ValidationException("Cidade e obrigatoria.");
    }
}
