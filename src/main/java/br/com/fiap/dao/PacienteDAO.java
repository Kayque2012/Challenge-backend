package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.entities.Paciente;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteDAO {

    public void inserir(Paciente p) throws SQLException {
        String sql = "INSERT INTO T_SN_PACIENTE " +
                "(NOME_PACIENTE, EMAIL, SENHA, CPF, TIPO_PERFIL, DATA_NASCIMENTO, GENERO, PAIS, CIDADE, ESTADO, " +
                "RENDA_SALARIO_MINIMO, DESCRICAO_PROBLEMA, TELEFONE, ID_DENTISTA_ADOTANTE, TIPO_DOR, TEMPO_DOR_DIAS, URGENCIA, CRIADO_EM) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID_PACIENTE"})) {

            ps.setString(1, p.getNomePaciente());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getSenha());
            ps.setString(4, p.getCpf());
            ps.setString(5, p.getTipoPerfil() != null ? p.getTipoPerfil() : "paciente");
            ps.setDate(6, p.getDataNascimento() != null ? Date.valueOf(p.getDataNascimento()) : null);
            ps.setString(7, p.getGenero());
            ps.setString(8, p.getPais());
            ps.setString(9, p.getCidade());
            ps.setString(10, p.getEstado());
            ps.setDouble(11, p.getRendaSalarioMinimo());
            ps.setString(12, p.getDescricaoProblema());
            ps.setString(13, p.getTelefone());
            if (p.getIdDentistaAdotante() != null) ps.setInt(14, p.getIdDentistaAdotante()); else ps.setNull(14, java.sql.Types.INTEGER);
            ps.setString(15, p.getTipoDor());
            ps.setInt(16, p.getTempoDorDias());
            ps.setString(17, p.getUrgencia());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    public List<Paciente> listarTodos() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_PACIENTE WHERE STATUS_ATIVO = 'S' ORDER BY ID_PACIENTE";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Paciente> listarInativos() throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_PACIENTE WHERE STATUS_ATIVO = 'N' ORDER BY INATIVADO_EM DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public List<Paciente> listarPorCidade(String cidade) throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_PACIENTE WHERE UPPER(CIDADE) = UPPER(?) AND STATUS_ATIVO = 'S' AND TIPO_DOR IS NOT NULL AND ID_DENTISTA_ADOTANTE IS NULL ORDER BY ID_PACIENTE";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cidade);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Paciente> listarAdotadosPorDentista(int idDentista) throws SQLException {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_PACIENTE WHERE ID_DENTISTA_ADOTANTE = ? AND STATUS_ATIVO = 'S' ORDER BY ID_PACIENTE";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDentista);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public Paciente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM T_SN_PACIENTE WHERE ID_PACIENTE = ? AND STATUS_ATIVO = 'S'";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public Paciente buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM T_SN_PACIENTE WHERE UPPER(EMAIL) = UPPER(?) AND STATUS_ATIVO = 'S'";

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
        String sql = "UPDATE T_SN_PACIENTE SET LATITUDE=?, LONGITUDE=? WHERE ID_PACIENTE=?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }

    public void atualizarSenha(int id, String novoHash) throws SQLException {
        String sql = "UPDATE T_SN_PACIENTE SET SENHA = ? WHERE ID_PACIENTE = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, novoHash);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public boolean existeEmail(String email) throws SQLException {
        String sql = "SELECT 1 FROM T_SN_PACIENTE WHERE UPPER(EMAIL) = UPPER(?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void atualizar(Paciente p) throws SQLException {
        String sql = "UPDATE T_SN_PACIENTE SET NOME_PACIENTE=?, EMAIL=?, CPF=?, " +
                "DATA_NASCIMENTO=?, GENERO=?, PAIS=?, CIDADE=?, ESTADO=?, RENDA_SALARIO_MINIMO=?, " +
                "DESCRICAO_PROBLEMA=?, TELEFONE=?, ID_DENTISTA_ADOTANTE=?, TIPO_DOR=?, TEMPO_DOR_DIAS=?, URGENCIA=? " +
                "WHERE ID_PACIENTE=?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNomePaciente());
            ps.setString(2, p.getEmail());
            ps.setString(3, p.getCpf());
            ps.setDate(4, p.getDataNascimento() != null ? Date.valueOf(p.getDataNascimento()) : null);
            ps.setString(5, p.getGenero());
            ps.setString(6, p.getPais());
            ps.setString(7, p.getCidade());
            ps.setString(8, p.getEstado());
            ps.setDouble(9, p.getRendaSalarioMinimo());
            ps.setString(10, p.getDescricaoProblema());
            ps.setString(11, p.getTelefone());
            if (p.getIdDentistaAdotante() != null) ps.setInt(12, p.getIdDentistaAdotante()); else ps.setNull(12, java.sql.Types.INTEGER);
            ps.setString(13, p.getTipoDor());
            ps.setInt(14, p.getTempoDorDias());
            ps.setString(15, p.getUrgencia());
            ps.setInt(16, p.getId());

            ps.executeUpdate();
        }
    }

    /**
     * Soft-delete atômico: cancela todas as ofertas pendentes/confirmadas do paciente
     * e então o inativa — tudo em uma única transação.
     * Garante que nenhuma oferta órfã fique visível na agenda do dentista.
     */
    public void deletar(int id) throws SQLException {
        String sqlCancelarOfertas =
            "UPDATE T_SN_OFERTA SET STATUS_ATIVO = 'N', INATIVADO_EM = SYSTIMESTAMP " +
            "WHERE ID_PACIENTE = ? AND STATUS IN ('pendente', 'confirmado') AND STATUS_ATIVO = 'S'";
        String sqlPaciente =
            "UPDATE T_SN_PACIENTE SET STATUS_ATIVO = 'N', INATIVADO_EM = SYSTIMESTAMP " +
            "WHERE ID_PACIENTE = ?";

        try (Connection conn = ConnectionFactory.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps1 = conn.prepareStatement(sqlCancelarOfertas);
                 PreparedStatement ps2 = conn.prepareStatement(sqlPaciente)) {
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
        String sql = "UPDATE T_SN_PACIENTE SET STATUS_ATIVO = 'S', INATIVADO_EM = NULL WHERE ID_PACIENTE = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Paciente mapear(ResultSet rs) throws SQLException {
        Paciente p = new Paciente();
        p.setId(rs.getInt("ID_PACIENTE"));
        p.setNomePaciente(rs.getString("NOME_PACIENTE"));
        p.setEmail(rs.getString("EMAIL"));
        p.setSenha(rs.getString("SENHA"));
        p.setCpf(rs.getString("CPF"));
        p.setTipoPerfil(rs.getString("TIPO_PERFIL"));

        Date dtNasc = rs.getDate("DATA_NASCIMENTO");
        if (dtNasc != null) p.setDataNascimento(dtNasc.toLocalDate());

        p.setGenero(rs.getString("GENERO"));
        p.setPais(rs.getString("PAIS"));
        p.setCidade(rs.getString("CIDADE"));
        try { p.setEstado(rs.getString("ESTADO")); } catch (SQLException ignored) {}
        p.setRendaSalarioMinimo(rs.getDouble("RENDA_SALARIO_MINIMO"));

        p.setDescricaoProblema(rs.getString("DESCRICAO_PROBLEMA"));
        p.setTelefone(rs.getString("TELEFONE"));
        int idAdot = rs.getInt("ID_DENTISTA_ADOTANTE");
        p.setIdDentistaAdotante(rs.wasNull() ? null : idAdot);

        p.setTipoDor(rs.getString("TIPO_DOR"));
        p.setTempoDorDias(rs.getInt("TEMPO_DOR_DIAS"));
        p.setUrgencia(rs.getString("URGENCIA"));

        try { p.setLatitude(rs.getDouble("LATITUDE")); } catch (SQLException ignored) {}
        try { p.setLongitude(rs.getDouble("LONGITUDE")); } catch (SQLException ignored) {}

        Timestamp ts = rs.getTimestamp("CRIADO_EM");
        if (ts != null) p.setCriadoEm(ts.toLocalDateTime());

        return p;
    }
}
