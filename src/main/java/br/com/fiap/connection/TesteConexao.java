package br.com.fiap.connection;
import java.sql.Connection;
import java.sql.SQLException;

public class TesteConexao {
    public static void main(String[] args) {
        System.out.println("Tentando conectar ao banco de dados...");

        try {
            // Tenta obter a conexão usando a sua fábrica
            Connection conn = ConnectionFactory.getConnection();

            if (conn != null) {
                System.out.println(" Conexão estabelecida com sucesso!");
                System.out.println("O banco de dados está respondendo perfeitamente.");

                // Fecha a conexão após o teste
                ConnectionFactory.closeConnection(conn);
            }
        } catch (SQLException e) {
            System.err.println(" Erro ao conectar ao banco de dados!");
            System.err.println("Motivo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}