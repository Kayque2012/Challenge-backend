package br.com.fiap.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Fábrica de conexões JDBC para o banco Oracle da FIAP.
 *
 * As credenciais são lidas do application.properties via MicroProfile Config
 * (db.url, db.user, db.password), portanto nunca ficam hardcoded no código.
 *
 * getConnection() tenta até MAX_TENTATIVAS vezes com espera crescente entre elas,
 * o que absorve oscilações de rede no ambiente Azure → Oracle externo.
 *
 * closeConnection() é um helper opcional para blocos que não usam try-with-resources.
 */
public class ConnectionFactory {

    private static final int MAX_TENTATIVAS = 3;
    private static final long ESPERA_MS = 500; // espera inicial: 500ms, 1000ms, 1500ms...

    private static String cfg(String key) {
        return ConfigProvider.getConfig().getValue(key, String.class);
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver Oracle não encontrado: " + e.getMessage());
        }

        SQLException ultimoErro = null;
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                return DriverManager.getConnection(cfg("db.url"), cfg("db.user"), cfg("db.password"));
            } catch (SQLException e) {
                ultimoErro = e;
                if (tentativa < MAX_TENTATIVAS) {
                    try { Thread.sleep(ESPERA_MS * tentativa); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new SQLException("Falha ao conectar ao banco após " + MAX_TENTATIVAS + " tentativas: " + ultimoErro.getMessage(), ultimoErro);
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar conexão: " + e.getMessage());
            }
        }
    }
}
