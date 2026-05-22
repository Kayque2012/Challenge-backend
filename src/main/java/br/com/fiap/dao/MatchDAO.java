package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.entities.Match;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MatchDAO {

    public void inserir(Match m) throws SQLException {
        String sql = "INSERT INTO T_SN_MATCH " +
                "(DATA_SOLICITACAO, DATA_AGENDADA, STATUS, SCORE_MATCH, PRIORIDADE, " +
                "OBSERVACAO, CRIADO_EM, ID_AVALIACAO, ID_PACIENTE, ID_ESPECIALIDADE, ID_DENTISTA) " +
                "VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, m.getDataSolicitacao() != null ? Timestamp.valueOf(m.getDataSolicitacao()) : null);
            ps.setTimestamp(2, m.getDataAgendada() != null ? Timestamp.valueOf(m.getDataAgendada()) : null);
            ps.setString(3, m.getStatus());
            ps.setDouble(4, m.getScoreMatch());
            ps.setDouble(5, m.getPrioridade());
            ps.setString(6, m.getObservacao());
            if (m.getIdAvaliacao() != null) ps.setInt(7, m.getIdAvaliacao()); else ps.setNull(7, Types.NUMERIC);
            ps.setInt(8, m.getIdPaciente());
            ps.setInt(9, m.getIdEspecialidade());
            ps.setInt(10, m.getIdDentista());

            ps.executeUpdate();
            System.out.println("Match inserido com sucesso!");
        }
    }

    public List<Match> listarTodos() throws SQLException {
        List<Match> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_MATCH WHERE STATUS_ATIVO = 'S' ORDER BY ID_MATCH";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Match> listarInativos() throws SQLException {
        List<Match> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_MATCH WHERE STATUS_ATIVO = 'N' ORDER BY INATIVADO_EM DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Match buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM T_SN_MATCH WHERE ID_MATCH = ? AND STATUS_ATIVO = 'S'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void atualizar(Match m) throws SQLException {
        String sql = "UPDATE T_SN_MATCH SET DATA_SOLICITACAO=?, DATA_AGENDADA=?, STATUS=?, " +
                "SCORE_MATCH=?, PRIORIDADE=?, OBSERVACAO=?, ID_AVALIACAO=?, " +
                "ID_PACIENTE=?, ID_ESPECIALIDADE=?, ID_DENTISTA=? WHERE ID_MATCH=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, m.getDataSolicitacao() != null ? Timestamp.valueOf(m.getDataSolicitacao()) : null);
            ps.setTimestamp(2, m.getDataAgendada() != null ? Timestamp.valueOf(m.getDataAgendada()) : null);
            ps.setString(3, m.getStatus());
            ps.setDouble(4, m.getScoreMatch());
            ps.setDouble(5, m.getPrioridade());
            ps.setString(6, m.getObservacao());
            if (m.getIdAvaliacao() != null) ps.setInt(7, m.getIdAvaliacao()); else ps.setNull(7, Types.NUMERIC);
            ps.setInt(8, m.getIdPaciente());
            ps.setInt(9, m.getIdEspecialidade());
            ps.setInt(10, m.getIdDentista());
            ps.setInt(11, m.getId());

            int linhas = ps.executeUpdate();
            System.out.println(linhas > 0 ? "Match atualizado!" : "Match não encontrado.");
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "UPDATE T_SN_MATCH SET STATUS_ATIVO = 'N', INATIVADO_EM = SYSTIMESTAMP WHERE ID_MATCH = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void reativar(int id) throws SQLException {
        String sql = "UPDATE T_SN_MATCH SET STATUS_ATIVO = 'S', INATIVADO_EM = NULL WHERE ID_MATCH = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Match mapear(ResultSet rs) throws SQLException {
        Match m = new Match();
        m.setId(rs.getInt("ID_MATCH"));
        Timestamp sol = rs.getTimestamp("DATA_SOLICITACAO");
        if (sol != null) m.setDataSolicitacao(sol.toLocalDateTime());
        Timestamp ag = rs.getTimestamp("DATA_AGENDADA");
        if (ag != null) m.setDataAgendada(ag.toLocalDateTime());
        m.setStatus(rs.getString("STATUS"));
        m.setScoreMatch(rs.getDouble("SCORE_MATCH"));
        m.setPrioridade(rs.getDouble("PRIORIDADE"));
        m.setObservacao(rs.getString("OBSERVACAO"));
        Timestamp criado = rs.getTimestamp("CRIADO_EM");
        if (criado != null) m.setCriadoEm(criado.toLocalDateTime());
        int idAv = rs.getInt("ID_AVALIACAO");
        if (!rs.wasNull()) m.setIdAvaliacao(idAv);
        m.setIdPaciente(rs.getInt("ID_PACIENTE"));
        m.setIdEspecialidade(rs.getInt("ID_ESPECIALIDADE"));
        m.setIdDentista(rs.getInt("ID_DENTISTA"));
        return m;
    }
}
