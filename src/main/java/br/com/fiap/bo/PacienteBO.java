package br.com.fiap.bo;

import br.com.fiap.dao.PacienteDAO;
import br.com.fiap.entities.Paciente;
import br.com.fiap.exception.DatabaseException;
import br.com.fiap.exception.ResourceNotFoundException;
import br.com.fiap.exception.ValidationException;
import br.com.fiap.util.GeocodificacaoUtil;
import br.com.fiap.util.SenhaUtil;
import jakarta.enterprise.context.ApplicationScoped;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class PacienteBO {

    private final PacienteDAO dao = new PacienteDAO();

    public Paciente cadastrar(Paciente paciente) {
        validar(paciente);

        try {
            if (paciente.getEmail() != null && dao.existeEmail(paciente.getEmail())) {
                throw new ValidationException("E-mail ja cadastrado. Use outro e-mail ou faca login.");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao verificar e-mail.", e);
        }

        if (paciente.getSenha() != null && !paciente.getSenha().isBlank()
                && !SenhaUtil.eHashBcrypt(paciente.getSenha())) {
            paciente.setSenha(SenhaUtil.hashear(paciente.getSenha()));
        }

        if (paciente.getGenero() == null) paciente.setGenero("NAO_INFORMADO");
        if (paciente.getDescricaoProblema() == null) paciente.setDescricaoProblema("Cadastro inicial");
        if (paciente.getTipoPerfil() == null) paciente.setTipoPerfil("paciente");
        if (paciente.getDataNascimento() == null) {
            int idadeInformada = paciente.getIdade();
            paciente.setDataNascimento(
                java.time.LocalDate.now().minusYears(idadeInformada > 0 ? idadeInformada : 18)
            );
        }

        if (paciente.getTipoDor() != null && !paciente.getTipoDor().isBlank()) {
            paciente.setUrgencia(paciente.calcularUrgencia());
        } else {
            paciente.setTipoDor(null);
            paciente.setUrgencia("BAIXA");
        }

        try {
            dao.inserir(paciente);

            final int idGerado = paciente.getId();
            final String cidade = paciente.getCidade();
            final String estado = paciente.getEstado() != null ? paciente.getEstado() : "";
            final String pais = paciente.getPais() != null ? paciente.getPais() : "Brasil";
            new Thread(() -> {
                double[] coords = GeocodificacaoUtil.buscar(cidade, estado, pais);
                if (coords != null) {
                    try { dao.atualizarCoordenadas(idGerado, coords[0], coords[1]); }
                    catch (Exception ignored) {}
                }
            }).start();

            return paciente;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("ORA-00001")) {
                throw new ValidationException("CPF ou e-mail ja cadastrado no sistema.");
            }
            throw new DatabaseException("Erro ao cadastrar paciente.", e);
        }
    }

    public List<Paciente> listar() {
        try {
            return dao.listarTodos();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar pacientes.", e);
        }
    }

    public List<Paciente> listarPorCidade(String cidade) {
        return listar().stream()
                .filter(p -> cidade.equalsIgnoreCase(p.getCidade()))
                .collect(Collectors.toList());
    }

    public List<Paciente> listarAdotadosPorDentista(int idDentista) {
        try {
            return dao.listarAdotadosPorDentista(idDentista);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar pacientes adotados.", e);
        }
    }

    public Paciente buscarPorId(int id) {
        try {
            Paciente p = dao.buscarPorId(id);
            if (p == null) throw new ResourceNotFoundException("Paciente", id);
            return p;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao buscar paciente por ID.", e);
        }
    }

    public Paciente atualizar(int id, Paciente paciente) {
        try {
            Paciente existente = dao.buscarPorId(id);
            if (existente == null) throw new ResourceNotFoundException("Paciente", id);
            paciente.setId(id);

            if (paciente.getCpf() == null || paciente.getCpf().isBlank())
                paciente.setCpf(existente.getCpf());
            if (paciente.getGenero() == null || paciente.getGenero().isBlank())
                paciente.setGenero(existente.getGenero() != null ? existente.getGenero() : "NAO_INFORMADO");
            if (paciente.getIdade() > 0) {
                paciente.setDataNascimento(java.time.LocalDate.now().minusYears(paciente.getIdade()));
            } else if (paciente.getDataNascimento() == null) {
                paciente.setDataNascimento(existente.getDataNascimento());
            }
            if (paciente.getEmail() == null || paciente.getEmail().isBlank())
                paciente.setEmail(existente.getEmail());
            if (paciente.getCidade() == null || paciente.getCidade().isBlank()
                    || paciente.getCidade().equalsIgnoreCase("Não informada"))
                paciente.setCidade(existente.getCidade());
            if (paciente.getPais() == null || paciente.getPais().isBlank())
                paciente.setPais(existente.getPais());
            if (paciente.getTipoDor() == null || paciente.getTipoDor().isBlank())
                paciente.setTipoDor(existente.getTipoDor());
            if (paciente.getTempoDorDias() <= 0 && existente.getTempoDorDias() > 0)
                paciente.setTempoDorDias(existente.getTempoDorDias());

            String telefoneEfetivo = (paciente.getTelefone() != null && !paciente.getTelefone().isBlank())
                    ? paciente.getTelefone() : existente.getTelefone();
            String descBase = (paciente.getDescricaoProblema() != null && !paciente.getDescricaoProblema().isBlank())
                    ? paciente.getDescricaoProblema()
                    : (existente.getDescricaoProblema() != null ? existente.getDescricaoProblema() : "");
            String descComTel = (telefoneEfetivo != null && !telefoneEfetivo.isBlank())
                    ? "TEL:" + telefoneEfetivo + "|" + descBase : descBase;
            if ("adotado".equals(paciente.getStatus()) && paciente.getIdDentistaResponsavel() > 0) {
                paciente.setDescricaoProblema("ADOTADO:" + paciente.getIdDentistaResponsavel() + "|" + descComTel);
            } else {
                paciente.setDescricaoProblema(descComTel);
            }

            paciente.setUrgencia(paciente.calcularUrgencia());
            dao.atualizar(paciente);
            return paciente;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao atualizar paciente.", e);
        }
    }

    public void excluir(int id) {
        try {
            if (dao.buscarPorId(id) == null) throw new ResourceNotFoundException("Paciente", id);
            dao.deletar(id);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao excluir paciente.", e);
        }
    }

    public List<Paciente> listarInativos() {
        try {
            return dao.listarInativos();
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao listar pacientes inativos.", e);
        }
    }

    public void reativar(int id) {
        try {
            dao.reativar(id);
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao reativar paciente.", e);
        }
    }

    public int calcularIdade(int id) {
        return buscarPorId(id).calcularIdade();
    }

    public boolean verificarElegibilidade(int id) {
        return buscarPorId(id).verificarElegibilidade();
    }

    public void redefinirSenha(String email, String novaSenha) {
        if (email == null || email.isBlank())
            throw new ValidationException("E-mail e obrigatorio.");
        if (novaSenha == null || novaSenha.length() < 6)
            throw new ValidationException("A senha deve ter no minimo 6 caracteres.");
        try {
            Paciente p = dao.buscarPorEmail(email);
            if (p == null) throw new ValidationException("E-mail nao encontrado.");
            dao.atualizarSenha(p.getId(), SenhaUtil.hashear(novaSenha));
        } catch (ValidationException ve) {
            throw ve;
        } catch (SQLException e) {
            throw new DatabaseException("Erro ao redefinir senha.", e);
        }
    }

    private void validar(Paciente p) {
        if (p == null) throw new ValidationException("Paciente nao pode ser nulo.");
        if (p.getNomePaciente() == null || p.getNomePaciente().isBlank())
            throw new ValidationException("Nome do paciente e obrigatorio.");
        if (p.getEmail() == null || p.getEmail().isBlank())
            throw new ValidationException("Email e obrigatorio.");
        if (!p.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw new ValidationException("Formato de e-mail invalido.");
        if (p.getPais() == null || p.getPais().isBlank())
            throw new ValidationException("Pais e obrigatorio.");
        if (p.getCidade() == null || p.getCidade().isBlank())
            throw new ValidationException("Cidade e obrigatoria.");
        if (p.getRendaSalarioMinimo() < 0)
            throw new ValidationException("Renda nao pode ser negativa.");
        if (p.getTempoDorDias() < 0)
            throw new ValidationException("Tempo de dor nao pode ser negativo.");
    }
}
