package br.com.fiap.bo;

import br.com.fiap.dao.OfertaDAO;
import br.com.fiap.exception.DatabaseException;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OfertaBO {

    private final OfertaDAO dao = new OfertaDAO();

    public Map<String, Object> criar(int idDentista, int idPaciente, String procedimento,
                                     List<Map<String, String>> slots) {
        try {
            int id = dao.criarComSlots(idDentista, idPaciente, procedimento, slots);
            return Map.of(
                "id", id,
                "idDentista", idDentista,
                "idPaciente", idPaciente,
                "procedimento", procedimento != null ? procedimento : "",
                "status", "pendente"
            );
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao criar oferta de horário.", e);
        }
    }

    public Map<String, Object> buscarPendentePorPaciente(int idPaciente) {
        try {
            Map<String, Object> oferta = dao.buscarPendentePorPaciente(idPaciente);
            return oferta; // null if none
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar oferta do paciente.", e);
        }
    }

    public List<Map<String, Object>> buscarConfirmadosPorDentista(int idDentista) {
        try {
            return dao.buscarConfirmadosPorDentista(idDentista);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar agenda do dentista.", e);
        }
    }

    public List<Map<String, Object>> buscarTodosPorDentista(int idDentista) {
        try {
            return dao.buscarTodosPorDentista(idDentista);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar ofertas do dentista.", e);
        }
    }

    public void confirmar(int idOferta, String data, String hora) {
        try {
            dao.confirmar(idOferta, data, hora);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao confirmar oferta.", e);
        }
    }

    public void concluir(int idOferta) {
        try {
            dao.concluir(idOferta);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao concluir consulta.", e);
        }
    }

    public void cancelar(int idOferta) {
        try {
            dao.deletar(idOferta);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao cancelar oferta.", e);
        }
    }
}
