package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;

import java.sql.*;
import java.util.*;

public class OfertaDAO {

    public int inserir(int idDentista, int idPaciente, String procedimento) throws SQLException {
        String sql = "INSERT INTO T_SN_OFERTA (ID_DENTISTA, ID_PACIENTE, PROCEDIMENTO, STATUS) " +
                     "VALUES (?, ?, ?, 'pendente')";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_OFERTA"})) {

            ps.setInt(1, idDentista);
            ps.setInt(2, idPaciente);
            ps.setString(3, procedimento);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Falha ao obter ID da oferta inserida");
                return keys.getInt(1);
            }
        }
    }

    public void inserirSlots(Connection conn, int idOferta, List<Map<String, String>> slots) throws SQLException {
        String sql = "INSERT INTO T_SN_OFERTA_SLOT (ID_OFERTA, DATA_SLOT, HORA_SLOT) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Map<String, String> slot : slots) {
                ps.setInt(1, idOferta);
                ps.setString(2, slot.get("data"));
                ps.setString(3, slot.get("hora"));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int criarComSlots(int idDentista, int idPaciente, String procedimento,
                              List<Map<String, String>> slots) throws SQLException {

        String sqlOferta = "INSERT INTO T_SN_OFERTA (ID_DENTISTA, ID_PACIENTE, PROCEDIMENTO, STATUS) " +
                           "VALUES (?, ?, ?, 'pendente')";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int idOferta;
                try (PreparedStatement ps = conn.prepareStatement(sqlOferta, new String[]{"ID_OFERTA"})) {
                    ps.setInt(1, idDentista);
                    ps.setInt(2, idPaciente);
                    ps.setString(3, procedimento);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Falha ao obter ID da oferta");
                        idOferta = keys.getInt(1);
                    }
                }
                inserirSlots(conn, idOferta, slots);
                conn.commit();
                return idOferta;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public Map<String, Object> buscarPendentePorPaciente(int idPaciente) throws SQLException {
        String sqlOferta =
            "SELECT o.ID_OFERTA, o.PROCEDIMENTO, o.STATUS, o.SLOT_DATA, o.SLOT_HORA, " +
            "       o.CRIADO_EM, d.NOME_DENTISTA, d.CIDADE AS DENTISTA_CIDADE, d.PAIS AS DENTISTA_PAIS " +
            "  FROM T_SN_OFERTA o " +
            "  JOIN T_SN_DENTISTA d ON o.ID_DENTISTA = d.ID_DENTISTA " +
            " WHERE o.ID_PACIENTE = ? AND o.STATUS IN ('pendente', 'confirmado') AND o.STATUS_ATIVO = 'S' " +
            " ORDER BY CASE o.STATUS WHEN 'pendente' THEN 0 ELSE 1 END, o.CRIADO_EM DESC " +
            " FETCH FIRST 1 ROWS ONLY";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOferta)) {

            ps.setInt(1, idPaciente);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                int idOferta = rs.getInt("ID_OFERTA");
                Map<String, Object> oferta = new LinkedHashMap<>();
                String status = rs.getString("STATUS");
                String slotData = rs.getString("SLOT_DATA");
                String slotHora = rs.getString("SLOT_HORA");
                Map<String, String> slotEscolhido = null;
                if ("confirmado".equals(status) && slotData != null) {
                    slotEscolhido = new LinkedHashMap<>();
                    slotEscolhido.put("data", slotData);
                    slotEscolhido.put("hora", slotHora != null ? slotHora : "");
                }
                oferta.put("id", idOferta);
                oferta.put("dentistaNome", rs.getString("NOME_DENTISTA"));
                oferta.put("dentistaCidade", rs.getString("DENTISTA_CIDADE"));
                oferta.put("dentistaPais", rs.getString("DENTISTA_PAIS") != null ? rs.getString("DENTISTA_PAIS") : "Brasil");
                oferta.put("procedimento", rs.getString("PROCEDIMENTO"));
                oferta.put("status", status);
                oferta.put("slotEscolhido", slotEscolhido);
                Timestamp criadoEm = rs.getTimestamp("CRIADO_EM");
                oferta.put("criadoEm", criadoEm != null ? criadoEm.toLocalDateTime().toString() : "");
                oferta.put("slots", buscarSlots(conn, idOferta));
                return oferta;
            }
        }
    }

    public List<Map<String, Object>> buscarConfirmadosPorDentista(int idDentista) throws SQLException {
        String sql =
            "SELECT o.ID_OFERTA, o.ID_PACIENTE, p.NOME_PACIENTE, " +
            "       o.PROCEDIMENTO, o.STATUS, o.SLOT_DATA, o.SLOT_HORA, o.CRIADO_EM " +
            "  FROM T_SN_OFERTA o " +
            "  JOIN T_SN_PACIENTE p ON o.ID_PACIENTE = p.ID_PACIENTE " +
            " WHERE o.ID_DENTISTA = ? AND o.STATUS = 'confirmado' AND o.STATUS_ATIVO = 'S' " +
            " ORDER BY o.SLOT_DATA, o.SLOT_HORA";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDentista);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ID_OFERTA"));
                    row.put("idPaciente", rs.getInt("ID_PACIENTE"));
                    row.put("pacienteNome", rs.getString("NOME_PACIENTE"));
                    row.put("procedimento", rs.getString("PROCEDIMENTO"));
                    row.put("status", rs.getString("STATUS"));
                    row.put("data", rs.getString("SLOT_DATA"));
                    row.put("hora", rs.getString("SLOT_HORA"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    public List<Map<String, Object>> buscarTodosPorDentista(int idDentista) throws SQLException {
        String sql =
            "SELECT o.ID_OFERTA, o.ID_PACIENTE, p.NOME_PACIENTE, " +
            "       o.PROCEDIMENTO, o.STATUS, o.SLOT_DATA, o.SLOT_HORA, o.CRIADO_EM " +
            "  FROM T_SN_OFERTA o " +
            "  JOIN T_SN_PACIENTE p ON o.ID_PACIENTE = p.ID_PACIENTE " +
            " WHERE o.ID_DENTISTA = ? AND o.STATUS_ATIVO = 'S' " +
            " ORDER BY o.CRIADO_EM DESC";

        List<Map<String, Object>> result = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idDentista);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("id", rs.getInt("ID_OFERTA"));
                    row.put("idPaciente", rs.getInt("ID_PACIENTE"));
                    row.put("pacienteNome", rs.getString("NOME_PACIENTE"));
                    row.put("procedimento", rs.getString("PROCEDIMENTO"));
                    row.put("status", rs.getString("STATUS"));
                    row.put("data", rs.getString("SLOT_DATA"));
                    row.put("hora", rs.getString("SLOT_HORA"));
                    result.add(row);
                }
            }
        }
        return result;
    }

    public void confirmar(int idOferta, String data, String hora) throws SQLException {
        String sql = "UPDATE T_SN_OFERTA SET STATUS='confirmado', SLOT_DATA=?, SLOT_HORA=? " +
                     "WHERE ID_OFERTA=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, data);
            ps.setString(2, hora);
            ps.setInt(3, idOferta);
            ps.executeUpdate();
        }
    }

    public void concluir(int idOferta) throws SQLException {
        String sql = "UPDATE T_SN_OFERTA SET STATUS='concluido' " +
                     "WHERE ID_OFERTA=? AND STATUS='confirmado'";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Oferta nao encontrada ou nao esta confirmada.");
        }
    }

    public void deletar(int idOferta) throws SQLException {
        String sql = "UPDATE T_SN_OFERTA SET STATUS_ATIVO = 'N', INATIVADO_EM = SYSTIMESTAMP WHERE ID_OFERTA = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> listarInativos() throws SQLException {
        String sql = "SELECT o.ID_OFERTA, o.ID_DENTISTA, o.ID_PACIENTE, o.PROCEDIMENTO, " +
                     "       o.STATUS, o.CRIADO_EM, o.INATIVADO_EM " +
                     "  FROM T_SN_OFERTA o WHERE o.STATUS_ATIVO = 'N' ORDER BY o.INATIVADO_EM DESC";
        List<Map<String, Object>> result = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("ID_OFERTA"));
                row.put("idDentista", rs.getInt("ID_DENTISTA"));
                row.put("idPaciente", rs.getInt("ID_PACIENTE"));
                row.put("procedimento", rs.getString("PROCEDIMENTO"));
                row.put("status", rs.getString("STATUS"));
                Timestamp criadoEm = rs.getTimestamp("CRIADO_EM");
                row.put("criadoEm", criadoEm != null ? criadoEm.toLocalDateTime().toString() : "");
                Timestamp inativadoEm = rs.getTimestamp("INATIVADO_EM");
                row.put("inativadoEm", inativadoEm != null ? inativadoEm.toLocalDateTime().toString() : "");
                result.add(row);
            }
        }
        return result;
    }

    public void reativar(int idOferta) throws SQLException {
        String sql = "UPDATE T_SN_OFERTA SET STATUS_ATIVO = 'S', INATIVADO_EM = NULL WHERE ID_OFERTA = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            ps.executeUpdate();
        }
    }

    private List<Map<String, Object>> buscarSlots(Connection conn, int idOferta) throws SQLException {
        String sql = "SELECT ID_SLOT, DATA_SLOT, HORA_SLOT FROM T_SN_OFERTA_SLOT " +
                     "WHERE ID_OFERTA=? ORDER BY DATA_SLOT, HORA_SLOT";

        List<Map<String, Object>> slots = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> slot = new LinkedHashMap<>();
                    slot.put("id", String.valueOf(rs.getInt("ID_SLOT")));
                    slot.put("data", rs.getString("DATA_SLOT"));
                    slot.put("hora", rs.getString("HORA_SLOT"));
                    slots.add(slot);
                }
            }
        }
        return slots;
    }
}
