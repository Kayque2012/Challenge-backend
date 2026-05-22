package br.com.fiap.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilitário de hash de senha usando BCrypt.
 *
 * Custo 12 = ~300ms por operação em hardware moderno.
 * Lento o suficiente para dificultar força-bruta; rápido o suficiente para o usuário.
 *
 * Suporta modo dual: senhas antigas em texto plano continuam funcionando
 * enquanto são migradas automaticamente para BCrypt no primeiro login.
 */
public class SenhaUtil {

    private static final int CUSTO = 12;

    private SenhaUtil() {}

    /**
     * Gera o hash BCrypt de uma senha em texto plano.
     * Cada chamada gera um salt aleatório diferente — hashes distintos para a mesma senha.
     */
    public static String hashear(String senhaPlana) {
        if (senhaPlana == null || senhaPlana.isBlank()) {
            throw new IllegalArgumentException("Senha nao pode ser nula ou vazia.");
        }
        return BCrypt.hashpw(senhaPlana, BCrypt.gensalt(CUSTO));
    }

    /**
     * Verifica se a senha digitada corresponde ao valor armazenado.
     *
     * Modo dual (compatibilidade retroativa):
     *   - Se o hash começa com "$2" → é BCrypt → usa BCrypt.checkpw()
     *   - Caso contrário → é texto plano (usuário antigo) → comparação direta
     *
     * Após a migração automática no primeiro login (feita no AuthBO),
     * todos os registros passarão a ter hash BCrypt e o ramo de texto
     * plano nunca mais será atingido.
     */
    public static boolean verificar(String senhaPlana, String valorArmazenado) {
        if (senhaPlana == null || valorArmazenado == null) return false;
        if (eHashBcrypt(valorArmazenado)) {
            return BCrypt.checkpw(senhaPlana, valorArmazenado);
        }
        // Fallback: senha ainda em texto plano (usuário cadastrado antes do hash)
        return senhaPlana.equals(valorArmazenado);
    }

    /**
     * Retorna true se o valor armazenado já é um hash BCrypt.
     * Usado pelo AuthBO para decidir se deve migrar o registro.
     */
    public static boolean eHashBcrypt(String valorArmazenado) {
        return valorArmazenado != null && valorArmazenado.startsWith("$2");
    }
}
