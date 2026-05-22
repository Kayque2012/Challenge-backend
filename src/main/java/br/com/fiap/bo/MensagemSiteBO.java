package br.com.fiap.bo;

import br.com.fiap.dao.MensagemSiteDAO;
import br.com.fiap.entities.MensagemSite;
import br.com.fiap.exception.DatabaseException;
import br.com.fiap.exception.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.util.List;

@ApplicationScoped
public class MensagemSiteBO {

    private final MensagemSiteDAO dao = new MensagemSiteDAO();

    public MensagemSite registrar(MensagemSite m) {
        validar(m);
        try {
            dao.inserir(m);
            return m;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao registrar mensagem.", e);
        }
    }

    public List<MensagemSite> listar() {
        try {
            return dao.listarTodas();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar mensagens.", e);
        }
    }

    public void excluir(int id) {
        try {
            dao.deletar(id);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao excluir mensagem.", e);
        }
    }

    private void validar(MensagemSite m) {
        if (m == null) throw new ValidationException("Mensagem nao pode ser nula.");
        if (m.getNome() == null || m.getNome().isBlank())
            throw new ValidationException("Nome e obrigatorio.");
        if (m.getEmail() == null || m.getEmail().isBlank())
            throw new ValidationException("Email e obrigatorio.");
        if (m.getAssunto() == null || m.getAssunto().isBlank())
            throw new ValidationException("Assunto e obrigatorio.");
        if (m.getMensagem() == null || m.getMensagem().isBlank())
            throw new ValidationException("Mensagem e obrigatoria.");
    }
}
