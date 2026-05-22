package br.com.fiap.dao;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.entities.Historico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoricoDAO {

    public List<Historico> listarPorPaciente(int idPaciente) throws SQLException {
        List<Historico> lista = new ArrayList<>();
        String sql = "SELECT * FROM T_SN_HISTORICO WHERE ID_PACIENTE = ? ORDER BY ID_HISTORICO DESC";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPaciente);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Historico h = new Historico();
                    h.setId(rs.getInt("ID_HISTORICO"));
                    h.setIdPaciente(rs.getInt("ID_PACIENTE"));
                    h.setTitulo(rs.getString("TITULO"));
                    h.setStatus(rs.getString("STATUS"));
                    h.setData(rs.getString("DATA_CONSULTA"));
                    h.setHora(rs.getString("HORA_CONSULTA"));
                    h.setProc(rs.getString("PROCEDIMENTO"));
                    h.setDentista(rs.getString("DENTISTA"));
                    lista.add(h);
                }
            }
        }
        return lista;
    }
}