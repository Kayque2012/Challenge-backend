package br.com.fiap.bo;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.exception.DatabaseException;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Camada de regras de negocio para o historico de atendimentos de um paciente.
 *
 * Consumido pelo PacienteDashboard.tsx do front, que espera receber um array
 * com os campos: id, titulo, status, data, hora, proc, dentista.
 *
 * Fonte de dados: T_SN_OFERTA (STATUS 'confirmado' ou 'concluido').
 * Isso evita dependencia de T_SN_MATCH/T_SN_ATENDIMENTO e mantem
 * T_SN_OFERTA como unica fonte de verdade do agendamento.
 */
@ApplicationScoped
public class HistoricoBO {

    public List<Map<String, Object>> buscarHistoricoPorNome(String nomePaciente) {
        String sql =
            "SELECT o.ID_OFERTA, o.PROCEDIMENTO, o.STATUS, o.SLOT_DATA, o.SLOT_HORA, " +
            "       d.NOME_DENTISTA " +
            "  FROM T_SN_OFERTA o " +
            "  JOIN T_SN_DENTISTA d ON o.ID_DENTISTA = d.ID_DENTISTA " +
            "  JOIN T_SN_PACIENTE p ON o.ID_PACIENTE = p.ID_PACIENTE " +
            " WHERE UPPER(p.NOME_PACIENTE) = UPPER(?) " +
            "   AND o.STATUS IN ('confirmado', 'concluido') " +
            " ORDER BY o.SLOT_DATA DESC, o.SLOT_HORA DESC";

        return executarQuery(sql, ps -> ps.setString(1, nomePaciente));
    }

    public List<Map<String, Object>> buscarHistoricoPorId(int idPaciente) {
        String sql =
            "SELECT o.ID_OFERTA, o.PROCEDIMENTO, o.STATUS, o.SLOT_DATA, o.SLOT_HORA, " +
            "       d.NOME_DENTISTA " +
            "  FROM T_SN_OFERTA o " +
            "  JOIN T_SN_DENTISTA d ON o.ID_DENTISTA = d.ID_DENTISTA " +
            " WHERE o.ID_PACIENTE = ? " +
            "   AND o.STATUS IN ('confirmado', 'concluido') " +
            " ORDER BY o.SLOT_DATA DESC, o.SLOT_HORA DESC";

        return executarQuery(sql, ps -> ps.setInt(1, idPaciente));
    }

    private List<Map<String, Object>> executarQuery(String sql, ParametroSetter setter) {
        List<Map<String, Object>> resultado = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setter.setar(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> consulta = new LinkedHashMap<>();
                    String proc   = rs.getString("PROCEDIMENTO");
                    String status = rs.getString("STATUS");
                    String slotData = rs.getString("SLOT_DATA");
                    String slotHora = rs.getString("SLOT_HORA");

                    consulta.put("id",      rs.getInt("ID_OFERTA"));
                    consulta.put("titulo",  proc != null ? proc : "Consulta");
                    consulta.put("status",  "concluido".equals(status) ? "Concluído" : "Agendado");
                    consulta.put("proc",    proc != null ? proc : "");
                    consulta.put("dentista", rs.getString("NOME_DENTISTA"));
                    consulta.put("data",    formatarData(slotData));
                    consulta.put("hora",    slotHora != null ? slotHora : "");
                    resultado.add(consulta);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar historico do paciente.", e);
        }
        return resultado;
    }

    /** Converte 'yyyy-MM-dd' para 'dd/MM/yyyy'. */
    private String formatarData(String slotData) {
        if (slotData == null || slotData.length() < 10) return "";
        return slotData.substring(8, 10) + "/" + slotData.substring(5, 7) + "/" + slotData.substring(0, 4);
    }

    @FunctionalInterface
    private interface ParametroSetter {
        void setar(PreparedStatement ps) throws SQLException;
    }
}
