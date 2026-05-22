package br.com.fiap.bo;

import br.com.fiap.connection.ConnectionFactory;
import br.com.fiap.dao.DentistaDAO;
import br.com.fiap.dao.PacienteDAO;
import br.com.fiap.entities.Dentista;
import br.com.fiap.entities.Paciente;
import br.com.fiap.exception.DatabaseException;
import br.com.fiap.util.GeocodificacaoUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RelatorioBO {

    private final PacienteDAO pacienteDAO = new PacienteDAO();
    private final DentistaDAO dentistaDAO = new DentistaDAO();

    public Map<String, Object> gerarEstatisticasAdmin() {
        Map<String, Object> stats = new LinkedHashMap<>();

        try {
            List<Paciente> pacientes = pacienteDAO.listarTodos();
            List<Dentista> dentistas = dentistaDAO.listarTodos();

            stats.put("total_beneficiarios", pacientes.size());
            stats.put("total_dentistas", dentistas.size());

            // Agrupa pacientes E dentistas por cidade para cobrir todas as regiões ativas
            stats.put("por_cidade", contarRegistrosPorCidade(pacientes, dentistas));

            stats.put("coordenadas", coletarCoordenadas(pacientes, dentistas));

            try {
                stats.put("ultimos_agendamentos", buscarUltimosAgendamentos());
            } catch (SQLException e) {
                stats.put("ultimos_agendamentos", new ArrayList<>());
            }

            return stats;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao gerar estatisticas administrativas.", e);
        }
    }

    private List<Map<String, String>> buscarUltimosAgendamentos() throws SQLException {
        List<Map<String, String>> lista = new ArrayList<>();
        // Lê de T_SN_OFERTA (fonte de verdade) — T_SN_ATENDIMENTO não é usado no fluxo atual
        String sql = "SELECT NOME_PACIENTE, URGENCIA, PROCEDIMENTO, NOME_DENTISTA, CIDADE, SLOT_DATA, SLOT_HORA " +
                "FROM ( " +
                "  SELECT p.NOME_PACIENTE, p.URGENCIA, o.PROCEDIMENTO, " +
                "         d.NOME_DENTISTA, d.CIDADE, o.SLOT_DATA, o.SLOT_HORA, " +
                "         ROW_NUMBER() OVER (PARTITION BY o.ID_PACIENTE ORDER BY o.SLOT_DATA DESC, o.SLOT_HORA DESC) AS rn " +
                "  FROM T_SN_OFERTA o " +
                "  JOIN T_SN_DENTISTA d ON o.ID_DENTISTA = d.ID_DENTISTA " +
                "  JOIN T_SN_PACIENTE p ON o.ID_PACIENTE = p.ID_PACIENTE " +
                "  WHERE o.STATUS IN ('confirmado', 'concluido') " +
                ") WHERE rn = 1 " +
                "ORDER BY SLOT_DATA DESC, SLOT_HORA DESC " +
                "FETCH FIRST 5 ROWS ONLY";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String urgencia = rs.getString("URGENCIA");
                String prioridade = "ALTA".equals(urgencia) ? "Urgente" : "MEDIA".equals(urgencia) ? "Alta" : "Normal";
                String slotData = rs.getString("SLOT_DATA");
                String dataFormatada = "-";
                if (slotData != null && !slotData.isBlank()) {
                    try {
                        java.time.LocalDate ld = java.time.LocalDate.parse(slotData);
                        dataFormatada = ld.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception ignored) {
                        dataFormatada = slotData;
                    }
                }
                Map<String, String> ag = new LinkedHashMap<>();
                ag.put("paciente", rs.getString("NOME_PACIENTE"));
                ag.put("prioridade", prioridade);
                ag.put("proc", rs.getString("PROCEDIMENTO"));
                ag.put("dentista", rs.getString("NOME_DENTISTA"));
                ag.put("data", dataFormatada);
                ag.put("hora", rs.getString("SLOT_HORA") != null ? rs.getString("SLOT_HORA") : "-");
                ag.put("cidade", rs.getString("CIDADE"));
                lista.add(ag);
            }
        }
        return lista;
    }

    private Map<String, Integer> contarRegistrosPorCidade(List<Paciente> pacientes, List<Dentista> dentistas) {
        Map<String, Integer> contagem = new LinkedHashMap<>();
        for (Paciente p : pacientes) {
            String cidade = (p.getCidade() != null && !p.getCidade().isBlank()) ? p.getCidade() : "Não informado";
            contagem.merge(cidade, 1, Integer::sum);
        }
        for (Dentista d : dentistas) {
            String cidade = (d.getCidade() != null && !d.getCidade().isBlank()) ? d.getCidade() : "Não informado";
            contagem.merge(cidade, 1, Integer::sum);
        }
        return contagem;
    }

    public long contarPacientesElegiveis() {
        try {
            return pacienteDAO.listarTodos().stream()
                    .filter(Paciente::verificarElegibilidade)
                    .count();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao contar pacientes elegiveis.", e);
        }
    }

    /**
     * Lê coordenadas salvas no banco (coluna LATITUDE/LONGITUDE).
     * Para registros antigos sem coordenadas, chama Nominatim como fallback único
     * e persiste o resultado para que futuras chamadas sejam instantâneas.
     */
    private Map<String, List<Double>> coletarCoordenadas(List<Paciente> pacientes, List<Dentista> dentistas) {
        Map<String, List<Double>> resultado = new LinkedHashMap<>();
        // cidade → [estado, pais] para uso no fallback Nominatim
        Map<String, String[]> semCoords = new LinkedHashMap<>();

        for (Paciente p : pacientes) {
            if (p.getCidade() == null || p.getCidade().isBlank()) continue;
            if (p.getLatitude() != 0.0 || p.getLongitude() != 0.0) {
                resultado.putIfAbsent(p.getCidade(), Arrays.asList(p.getLatitude(), p.getLongitude()));
            } else {
                semCoords.putIfAbsent(p.getCidade(), new String[]{
                    p.getEstado() != null ? p.getEstado() : "",
                    p.getPais() != null ? p.getPais() : "Brasil"
                });
            }
        }
        for (Dentista d : dentistas) {
            if (d.getCidade() == null || d.getCidade().isBlank()) continue;
            if (d.getLatitude() != 0.0 || d.getLongitude() != 0.0) {
                resultado.putIfAbsent(d.getCidade(), Arrays.asList(d.getLatitude(), d.getLongitude()));
            } else {
                semCoords.putIfAbsent(d.getCidade(), new String[]{
                    d.getEstado() != null ? d.getEstado() : "",
                    d.getPais() != null ? d.getPais() : "Brasil"
                });
            }
        }

        // Fallback para registros antigos: chama Nominatim e persiste resultado no banco
        for (Map.Entry<String, String[]> entry : semCoords.entrySet()) {
            if (resultado.containsKey(entry.getKey())) continue;
            String[] estadoPais = entry.getValue();
            double[] coords = GeocodificacaoUtil.buscar(entry.getKey(), estadoPais[0], estadoPais[1]);
            if (coords != null) {
                resultado.put(entry.getKey(), Arrays.asList(coords[0], coords[1]));
                persistirCoordenadasAntigos(entry.getKey(), coords);
            }
        }

        return resultado;
    }

    private void persistirCoordenadasAntigos(String cidade, double[] coords) {
        try {
            for (Paciente p : pacienteDAO.listarTodos()) {
                if (cidade.equalsIgnoreCase(p.getCidade()) && p.getLatitude() == 0.0 && p.getLongitude() == 0.0) {
                    pacienteDAO.atualizarCoordenadas(p.getId(), coords[0], coords[1]);
                }
            }
            for (Dentista d : dentistaDAO.listarTodos()) {
                if (cidade.equalsIgnoreCase(d.getCidade()) && d.getLatitude() == 0.0 && d.getLongitude() == 0.0) {
                    dentistaDAO.atualizarCoordenadas(d.getId(), coords[0], coords[1]);
                }
            }
        } catch (Exception ignored) {}
    }

    public long contarDentistasComVaga() {
        try {
            return dentistaDAO.listarTodos().stream()
                    .filter(Dentista::temVagaDisponivel)
                    .count();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao contar dentistas com vaga.", e);
        }
    }
}