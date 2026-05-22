package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.entities.MensagemSite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MensagemSiteDAO {

    public void inserir(MensagemSite m) throws SQLException {
        String sql = "INSERT INTO T_SN_MENSAGEM_SITE (NOME, EMAIL, ASSUNTO, MENSAGEM, DATA_ENVIO) " +
                "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, m.getNome());
            ps.setString(2, m.getEmail());
            ps.setString(3, m.getAssunto());
            ps.setString(4, m.getMensagem());

            ps.executeUpdate();
            System.out.println("Mensagem registrada com sucesso!");
        }
    }

    public List<MensagemSite> listarTodas() throws SQLException {
        List<MensagemSite> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_MENSAGEM_SITE ORDER BY DATA_ENVIO DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public MensagemSite buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM T_SN_MENSAGEM_SITE WHERE ID_MENSAGEM = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapear(rs);
            }
        }
        return null;
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM T_SN_MENSAGEM_SITE WHERE ID_MENSAGEM = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private MensagemSite mapear(ResultSet rs) throws SQLException {
        MensagemSite m = new MensagemSite();
        m.setId(rs.getInt("ID_MENSAGEM"));
        m.setNome(rs.getString("NOME"));
        m.setEmail(rs.getString("EMAIL"));
        m.setAssunto(rs.getString("ASSUNTO"));
        m.setMensagem(rs.getString("MENSAGEM"));
        Timestamp data = rs.getTimestamp("DATA_ENVIO");
        if (data != null) m.setDataEnvio(data.toLocalDateTime());
        return m;
    }
}
