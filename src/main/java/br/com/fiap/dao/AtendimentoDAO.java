package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.entities.Atendimento;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoDAO {

    public void inserir(Atendimento a) throws SQLException {
        String sql = "INSERT INTO T_SN_ATENDIMENTO " +
                "(DATA_INICIO, PROCEDIMENTO_REALIZADO, DIAGNOSTICO_FINAL, OBSERVACAO_CLINICA, " +
                "STATUS_ATENDIMENTO, ENCAMINHAMENTO, ID_MATCH, ID_DENTISTA) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, a.getDataInicio() != null ? Timestamp.valueOf(a.getDataInicio()) : null);
            ps.setString(2, a.getProcedimentoRealizado());
            ps.setString(3, a.getDiagnosticoFinal());
            ps.setString(4, a.getObservacaoClinica());
            ps.setString(5, a.getStatusAtendimento());
            ps.setString(6, a.getEncaminhamento());
            ps.setInt(7, a.getIdMatch());
            ps.setInt(8, a.getIdDentista());

            ps.executeUpdate();
            System.out.println("Atendimento inserido com sucesso!");
        }
    }

    public List<Atendimento> listarTodos() throws SQLException {
        List<Atendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_ATENDIMENTO WHERE STATUS_ATIVO = 'S' ORDER BY ID_ATENDIMENTO";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Atendimento> listarInativos() throws SQLException {
        List<Atendimento> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_ATENDIMENTO WHERE STATUS_ATIVO = 'N' ORDER BY INATIVADO_EM DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Atendimento buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM T_SN_ATENDIMENTO WHERE ID_ATENDIMENTO = ? AND STATUS_ATIVO = 'S'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void atualizar(Atendimento a) throws SQLException {
        String sql = "UPDATE T_SN_ATENDIMENTO SET DATA_INICIO=?, PROCEDIMENTO_REALIZADO=?, " +
                "DIAGNOSTICO_FINAL=?, OBSERVACAO_CLINICA=?, STATUS_ATENDIMENTO=?, " +
                "ENCAMINHAMENTO=?, ID_MATCH=?, ID_DENTISTA=? WHERE ID_ATENDIMENTO=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, a.getDataInicio() != null ? Timestamp.valueOf(a.getDataInicio()) : null);
            ps.setString(2, a.getProcedimentoRealizado());
            ps.setString(3, a.getDiagnosticoFinal());
            ps.setString(4, a.getObservacaoClinica());
            ps.setString(5, a.getStatusAtendimento());
            ps.setString(6, a.getEncaminhamento());
            ps.setInt(7, a.getIdMatch());
            ps.setInt(8, a.getIdDentista());
            ps.setInt(9, a.getId());

            int linhas = ps.executeUpdate();
            System.out.println(linhas > 0 ? "Atendimento atualizado!" : "Atendimento não encontrado.");
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "UPDATE T_SN_ATENDIMENTO SET STATUS_ATIVO = 'N', INATIVADO_EM = SYSTIMESTAMP WHERE ID_ATENDIMENTO = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void reativar(int id) throws SQLException {
        String sql = "UPDATE T_SN_ATENDIMENTO SET STATUS_ATIVO = 'S', INATIVADO_EM = NULL WHERE ID_ATENDIMENTO = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Atendimento mapear(ResultSet rs) throws SQLException {
        Atendimento a = new Atendimento();
        a.setId(rs.getInt("ID_ATENDIMENTO"));
        Timestamp dt = rs.getTimestamp("DATA_INICIO");
        if (dt != null) a.setDataInicio(dt.toLocalDateTime());
        a.setProcedimentoRealizado(rs.getString("PROCEDIMENTO_REALIZADO"));
        a.setDiagnosticoFinal(rs.getString("DIAGNOSTICO_FINAL"));
        a.setObservacaoClinica(rs.getString("OBSERVACAO_CLINICA"));
        a.setStatusAtendimento(rs.getString("STATUS_ATENDIMENTO"));
        a.setEncaminhamento(rs.getString("ENCAMINHAMENTO"));
        a.setIdMatch(rs.getInt("ID_MATCH"));
        a.setIdDentista(rs.getInt("ID_DENTISTA"));
        return a;
    }
}
