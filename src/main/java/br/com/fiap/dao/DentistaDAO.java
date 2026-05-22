package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.entities.Dentista;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DentistaDAO {

    public void inserir(Dentista d) throws SQLException {
        String sql = "INSERT INTO T_SN_DENTISTA " +
                "(NOME_DENTISTA, EMAIL, SENHA, CRO, TIPO_PERFIL, MAX_PACIENTES_MES, ATENDIDOS_MES, " +
                "PAIS, CIDADE, ESTADO, DESCRICAO_CLINICA, CRIADO_EM) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_DENTISTA"})) {

            ps.setString(1, d.getNomeDentista());
            ps.setString(2, d.getEmail());
            ps.setString(3, d.getSenha());
            ps.setString(4, d.getCro());
            ps.setString(5, d.getTipoPerfil() != null ? d.getTipoPerfil() : "dentista");
            ps.setInt(6, d.getMaxPacientesMes() > 0 ? d.getMaxPacientesMes() : 10);
            ps.setInt(7, 0);
            ps.setString(8, d.getPais());
            ps.setString(9, d.getCidade());
            ps.setString(10, d.getEstado());
            ps.setString(11, d.getDescricaoClinica());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) d.setId(keys.getInt(1));
            }
        }
    }

    public List<Dentista> listarTodos() throws SQLException {
        List<Dentista> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_DENTISTA WHERE STATUS_ATIVO = 'S' ORDER BY ID_DENTISTA";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Dentista> listarInativos() throws SQLException {
        List<Dentista> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_DENTISTA WHERE STATUS_ATIVO = 'N' ORDER BY INATIVADO_EM DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Dentista buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM T_SN_DENTISTA WHERE ID_DENTISTA = ? AND STATUS_ATIVO = 'S'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Dentista buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM T_SN_DENTISTA WHERE UPPER(EMAIL) = UPPER(?) AND STATUS_ATIVO = 'S'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void atualizarCoordenadas(int id, double latitude, double longitude) throws SQLException {
        String sql = "UPDATE T_SN_DENTISTA SET LATITUDE=?, LONGITUDE=? WHERE ID_DENTISTA=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void atualizarSenha(int id, String novoHash) throws SQLException {
        String sql = "UPDATE T_SN_DENTISTA SET SENHA = ? WHERE ID_DENTISTA = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM T_SN_DENTISTA WHERE UPPER(EMAIL) = UPPER(?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void atualizar(Dentista d) throws SQLException {
        String sql = "UPDATE T_SN_DENTISTA SET NOME_DENTISTA=?, EMAIL=?, CRO=?, " +
                "MAX_PACIENTES_MES=?, ATENDIDOS_MES=?, PAIS=?, CIDADE=?, ESTADO=?, DESCRICAO_CLINICA=? " +
                "WHERE ID_DENTISTA=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getNomeDentista());
            ps.setString(2, d.getEmail());
            ps.setString(3, d.getCro());
            ps.setInt(4, d.getMaxPacientesMes() > 0 ? d.getMaxPacientesMes() : 10);
            ps.setInt(5, d.getAtendidosMes());
            ps.setString(6, d.getPais());
            ps.setString(7, d.getCidade());
            ps.setString(8, d.getEstado());
            ps.setString(9, d.getDescricaoClinica());
            ps.setInt(10, d.getId());

            ps.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sqlPacientes = "UPDATE T_SN_PACIENTE SET ID_DENTISTA_ADOTANTE = NULL WHERE ID_DENTISTA_ADOTANTE = ?";
        String sqlDentista  = "UPDATE T_SN_DENTISTA SET STATUS_ATIVO = 'N', INATIVADO_EM = SYSTIMESTAMP WHERE ID_DENTISTA = ?";
        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlPacientes);
                 PreparedStatement ps2 = conn.prepareStatement(sqlDentista)) {
                ps1.setInt(1, id);
                ps1.executeUpdate();
                ps2.setInt(1, id);
                ps2.executeUpdate();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    public void reativar(int id) throws SQLException {
        String sql = "UPDATE T_SN_DENTISTA SET STATUS_ATIVO = 'S', INATIVADO_EM = NULL WHERE ID_DENTISTA = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Dentista mapear(ResultSet rs) throws SQLException {
        Dentista d = new Dentista();
        d.setId(rs.getInt("ID_DENTISTA"));
        d.setNomeDentista(rs.getString("NOME_DENTISTA"));
        d.setEmail(rs.getString("EMAIL"));
        d.setSenha(rs.getString("SENHA"));
        d.setCro(rs.getString("CRO"));
        d.setTipoPerfil(rs.getString("TIPO_PERFIL"));
        d.setMaxPacientesMes(rs.getInt("MAX_PACIENTES_MES"));
        d.setAtendidosMes(rs.getInt("ATENDIDOS_MES"));
        d.setPais(rs.getString("PAIS"));
        d.setCidade(rs.getString("CIDADE"));
        try { d.setEstado(rs.getString("ESTADO")); } catch (SQLException ignored) {}
        d.setDescricaoClinica(rs.getString("DESCRICAO_CLINICA"));

        try { d.setLatitude(rs.getDouble("LATITUDE")); } catch (SQLException ignored) {}
        try { d.setLongitude(rs.getDouble("LONGITUDE")); } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("CRIADO_EM");
        if (ts != null) d.setCriadoEm(ts.toLocalDateTime());

        return d;
    }
}
