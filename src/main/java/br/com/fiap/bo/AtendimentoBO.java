package br.com.fiap.bo;

import br.com.fiap.dao.AtendimentoDAO;
import br.com.fiap.entities.Atendimento;
import br.com.fiap.exception.DatabaseException;
import br.com.fiap.exception.ResourceNotFoundException;
import br.com.fiap.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class AtendimentoBO {

    private final AtendimentoDAO dao = new AtendimentoDAO();

    public Atendimento cadastrar(Atendimento atendimento) {
        validar(atendimento);
        try {
            dao.inserir(atendimento);
            return atendimento;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao cadastrar atendimento.", e);
        }
    }

    public List<Atendimento> listar() {
        try {
            return dao.listarTodos();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar atendimentos.", e);
        }
    }

    public Atendimento buscarPorId(int id) {
        try {
            Atendimento a = dao.buscarPorId(id);
            if (a == null) throw new ResourceNotFoundException("Atendimento", id);
            return a;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar atendimento.", e);
        }
    }

    public Atendimento atualizar(int id, Atendimento atendimento) {
        buscarPorId(id);
        validar(atendimento);
        atendimento.setId(id);
        try {
            dao.atualizar(atendimento);
            return atendimento;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar atendimento.", e);
        }
    }

    public void excluir(int id) {
        buscarPorId(id);
        try {
            dao.deletar(id);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao excluir atendimento.", e);
        }
    }

    public Atendimento concluir(int id) {
        Atendimento a = buscarPorId(id);
        if ("CONCLUIDO".equals(a.getStatusAtendimento()))
            throw new ValidationException("Atendimento já está concluído.");
        a.concluir();
        try {
            dao.atualizar(a);
            return a;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao concluir atendimento.", e);
        }
    }

    private void validar(Atendimento a) {
        if (a == null) throw new ValidationException("Atendimento não pode ser nulo.");
        if (a.getProcedimentoRealizado() == null || a.getProcedimentoRealizado().isBlank())
            throw new ValidationException("Procedimento realizado é obrigatório.");
        if (a.getStatusAtendimento() == null || a.getStatusAtendimento().isBlank())
            throw new ValidationException("Status do atendimento é obrigatório.");
        if (a.getIdDentista() <= 0)
            throw new ValidationException("ID do dentista é obrigatório.");
    }
}
