# 🦷 Dentista na Nuvem — Back-end API

> API RESTful em **Java 17 + Quarkus 3.8.5** para a plataforma odontológica Turma do Bem  
> **FIAP Challenge 2025 — Sprint 4 — Domain Driven Design Using Java**

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://adoptium.net)
[![Quarkus](https://img.shields.io/badge/Quarkus-3.8.5-blue?logo=quarkus)](https://quarkus.io)
[![Oracle](https://img.shields.io/badge/Oracle-19c-F80000?logo=oracle)](https://oracle.com)
[![Azure](https://img.shields.io/badge/Deploy-Azure-0078D4?logo=microsoftazure)](https://challengesprint-api.azurewebsites.net)

---

## 👥 Integrantes

| Nome | RM | Turma |
|------|----|-------|
| Eric Maciel | RM 567398 | 1TDSPB |
| Gabriel Correa | RM 567903 | 1TDSPB |
| Kayque Duarte Rodrigues | RM 567980 | 1TDSPB |

---

## 🌐 API em Produção

```
https://challengesprint-api.azurewebsites.net
```

**Swagger UI:**
```
https://challengesprint-api.azurewebsites.net/q/swagger-ui
```

**Health check:**
```
https://challengesprint-api.azurewebsites.net/q/health
```

---

## 🏗️ Arquitetura — Domain Driven Design

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Resource   │───▶│      BO      │───▶│     DAO      │───▶│    Entity    │
│  (REST API)  │    │  (Negócio)   │    │  (Acesso BD) │    │   (Modelo)   │
└──────────────┘    └──────────────┘    └──────────────┘    └──────────────┘
       │                   │
  JWT Auth            Validação
  @Path / @GET        BCrypt
  @POST / @PUT        Geocodificação
  @DELETE             Gemini IA
```

---

## 📁 Estrutura do Projeto

```
src/main/java/br/com/fiap/
├── entities/           # 8 classes de modelo (domínio)
│   ├── Paciente.java       — calcularUrgencia(), verificarElegibilidade(), calcularIdade()
│   ├── Dentista.java       — temVagaDisponivel()
│   ├── Match.java          — confirmar(), cancelar(), concluir(), isAtivo()
│   ├── Atendimento.java    — concluir(), cancelar(), isEmAndamento()
│   ├── Avaliacao.java      — notaValida(), classificar()
│   ├── Especialidade.java
│   ├── Oferta.java / OfertaSlot.java
│   ├── Historico.java
│   └── MensagemSite.java
│
├── dao/                # 7 DAOs — acesso ao banco via JDBC puro
│   ├── PacienteDAO.java
│   ├── DentistaDAO.java
│   ├── AtendimentoDAO.java
│   ├── MatchDAO.java
│   ├── OfertaDAO.java
│   ├── HistoricoDAO.java
│   └── MensagemSiteDAO.java
│
├── bo/                 # 8 Business Objects — regras de negócio
│   ├── PacienteBO.java
│   ├── DentistaBO.java
│   ├── AtendimentoBO.java
│   ├── AuthBO.java         — JWT + BCrypt + migração de senha
│   ├── OfertaBO.java
│   ├── HistoricoBO.java
│   ├── MensagemSiteBO.java
│   └── RelatorioBO.java
│
├── resource/           # 10 REST Resources — endpoints Quarkus
│   ├── AuthResource.java       — POST /login
│   ├── PacienteResource.java   — 12 endpoints
│   ├── DentistaResource.java   — 8 endpoints
│   ├── OfertaResource.java     — 7 endpoints
│   ├── AtendimentoResource.java— 6 endpoints
│   ├── HistoricoResource.java  — 1 endpoint
│   ├── MensagemSiteResource.java — 3 endpoints
│   ├── AdminResource.java      — 3 endpoints
│   ├── EmailResource.java      — 1 endpoint
│   └── IAResource.java         — 1 endpoint (rate limiting + cache)
│
├── connection/
│   └── ConnectionFactory.java  — JDBC com retry + backoff exponencial
│
├── util/
│   ├── SenhaUtil.java          — BCrypt hash/verify (custo 12)
│   └── GeocodificacaoUtil.java — Nominatim API com cache em memória
│
└── exception/
    ├── ValidationException.java
    ├── DatabaseException.java
    ├── ResourceNotFoundException.java
    └── GlobalExceptionMapper.java  — HTTP 400 / 404 / 500

src/main/resources/
└── application.properties      — configurações do banco e serviços

src/test/java/br/com/fiap/entities/
├── PacienteTest.java
├── DentistaTest.java
└── PacienteSoftDeleteTest.java

scriptatualizado.sql            — DDL Oracle completo (execute antes de rodar)
```

---

## ⚙️ Como Executar Localmente

### Pré-requisitos

| Ferramenta | Versão | Download |
|------------|--------|----------|
| Java JDK | 17+ | [adoptium.net](https://adoptium.net) |
| Apache Maven | 3.9+ | [maven.apache.org](https://maven.apache.org) |
| Oracle Database | 19c | Credenciais FIAP |
| Git | qualquer | [git-scm.com](https://git-scm.com) |

### 1. Clonar o repositório

```bash
git clone https://github.com/Kayque2012/Challenge-backend.git
cd Challenge-backend
```

### 2. Criar o banco de dados

Execute o script DDL no **Oracle SQL Developer** ou **SQL\*Plus**:

```sql
@scriptatualizado.sql
```

### 3. Configurar `application.properties`

Edite `src/main/resources/application.properties`:

```properties
# Banco Oracle FIAP
db.url=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL
db.user=SEU_RM
db.password=SUA_SENHA

# Gemini IA (opcional — triagem assistida)
gemini.api.key=SUA_CHAVE_GEMINI

# E-mail (opcional — confirmações e lembretes)
quarkus.mailer.host=smtp.gmail.com
quarkus.mailer.port=587
quarkus.mailer.username=seuemail@gmail.com
quarkus.mailer.password=sua_app_password
```

### 4. Iniciar em modo desenvolvimento (hot reload)

```bash
# Windows
mvnw.cmd quarkus:dev

# Linux / Mac
./mvnw quarkus:dev
```

### 5. Verificar

```bash
# Health check
curl http://localhost:8080/q/health

# Swagger UI
# Abra no navegador: http://localhost:8080/q/swagger-ui
```

---

## 🔑 Autenticação

O sistema usa **JWT Bearer Token** com validade de 24 horas.

### Obter token

```http
POST /login
Content-Type: application/json

{
  "email": "usuario@exemplo.com",
  "senha": "senha123"
}
```

**Resposta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "paciente",
  "nome": "João Silva",
  "id": 1
}
```

### Usar o token

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Roles disponíveis

| Role | Dashboard | Acesso |
|------|-----------|--------|
| `paciente` | /dashboard/paciente | Triagem, ofertas, histórico |
| `dentista` | /dashboard/dentista | Fila de pacientes, agenda |
| `admin` | /dashboard/admin | Estatísticas, relatórios |

---

## 📡 Endpoints

### Autenticação
| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| `POST` | `/login` | — | Autentica e retorna JWT |

### Pacientes
| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| `GET` | `/pacientes` | — | Lista pacientes ativos |
| `GET` | `/pacientes/inativos` | Sim | Lista pacientes inativos |
| `GET` | `/pacientes/{id}` | — | Busca por ID |
| `POST` | `/pacientes` | — | Cadastra paciente |
| `PUT` | `/pacientes/{id}` | Sim | Atualiza paciente |
| `DELETE` | `/pacientes/{id}` | Sim | Soft delete |
| `PATCH` | `/pacientes/{id}/reativar` | Sim | Reativa paciente |
| `GET` | `/pacientes/{id}/idade` | — | Retorna idade calculada |
| `GET` | `/pacientes/{id}/elegibilidade` | — | Verifica elegibilidade |
| `PUT` | `/pacientes/redefinir-senha` | — | Redefine senha |
| `GET` | `/pacientes/{id}/historico` | — | Histórico de atendimentos |

### Dentistas
| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| `GET` | `/dentistas` | — | Lista dentistas ativos |
| `GET` | `/dentistas/{id}` | — | Busca por ID |
| `POST` | `/dentistas` | — | Cadastra dentista |
| `PUT` | `/dentistas/{id}` | Sim | Atualiza dentista |
| `DELETE` | `/dentistas/{id}` | Sim | Soft delete em cascata |
| `PATCH` | `/dentistas/{id}/reativar` | Sim | Reativa dentista |
| `GET` | `/dentistas/{id}/vaga` | — | Verifica vagas disponíveis |

### Ofertas
| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| `POST` | `/ofertas` | Sim | Cria oferta com slots |
| `GET` | `/ofertas/paciente/{id}` | — | Oferta pendente do paciente |
| `GET` | `/ofertas/dentista/{id}` | Sim | Todas as ofertas do dentista |
| `GET` | `/ofertas/dentista/{id}/agenda` | Sim | Agenda (confirmadas) |
| `PUT` | `/ofertas/{id}/confirmar` | — | Paciente confirma horário |
| `PATCH` | `/ofertas/{id}/concluir` | Sim | Marca como concluída |
| `DELETE` | `/ofertas/{id}` | Sim | Cancela oferta |

### Atendimentos
| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| `GET` | `/atendimentos` | Sim | Lista atendimentos |
| `GET` | `/atendimentos/{id}` | Sim | Busca por ID |
| `POST` | `/atendimentos` | Sim | Registra atendimento |
| `PUT` | `/atendimentos/{id}` | Sim | Atualiza atendimento |
| `DELETE` | `/atendimentos/{id}` | Sim | Soft delete |
| `PATCH` | `/atendimentos/{id}/concluir` | Sim | Marca como concluído |

### Outros
| Método | URI | Auth | Descrição |
|--------|-----|------|-----------|
| `GET` | `/paciente/historico/{id}` | — | Histórico por ID ou nome |
| `GET` | `/mensagens` | Sim | Lista mensagens de contato |
| `POST` | `/mensagens` | — | Registra mensagem |
| `DELETE` | `/mensagens/{id}` | Sim | Remove mensagem |
| `GET` | `/admin/estatisticas` | Sim | Estatísticas do dashboard |
| `GET` | `/admin/elegiveis` | Sim | Contagem de elegíveis |
| `GET` | `/admin/dentistas-com-vaga` | Sim | Dentistas com vaga |
| `POST` | `/email/enviar` | Sim | Envia e-mail |
| `POST` | `/IA/consultar` | Sim | Triagem via Gemini IA |

> **Total: 48+ endpoints**

---

## 🧠 Lógica de Negócio (4 Métodos Principais)

```java
// 1. Calcular urgência do paciente para triagem prioritária
public String calcularUrgencia() {
    if (tipoDor == null) return "BAIXA";
    if (tipoDor.equalsIgnoreCase("dente quebrado")
            || tipoDor.equalsIgnoreCase("forte")
            || tempoDorDias > 7) return "ALTA";
    if (tipoDor.equalsIgnoreCase("moderada")) return "MEDIA";
    return "BAIXA";
}

// 2. Verificar elegibilidade ao programa Turma do Bem
public boolean verificarElegibilidade() {
    return calcularIdade() >= 11
        && calcularIdade() <= 17
        && rendaSalarioMinimo <= 3.0;
}

// 3. Verificar disponibilidade de vagas do dentista
public boolean temVagaDisponivel() {
    return this.atendidosMes < this.maxPacientesMes;
}

// 4. Classificar avaliação numérica (0–10) em categoria
public String classificar() {
    if (notaPacienteParaDentista >= 9) return "EXCELENTE";
    if (notaPacienteParaDentista >= 7) return "BOM";
    if (notaPacienteParaDentista >= 5) return "REGULAR";
    return "RUIM";
}
```

---

## 🗃️ Banco de Dados

### Tabelas Oracle

| Tabela | Linhas | Descrição |
|--------|--------|-----------|
| `T_SN_PACIENTE` | — | Jovens cadastrados + triagem |
| `T_SN_DENTISTA` | — | Dentistas voluntários |
| `T_SN_MATCH` | — | Pareamento paciente ↔ dentista |
| `T_SN_ATENDIMENTO` | — | Consultas realizadas (prontuário) |
| `T_SN_AVALIACAO` | — | Avaliações dos pacientes |
| `T_SN_ESPECIALIDADE` | — | Especialidades odontológicas |
| `T_SN_OFERTA` | — | Ofertas de consulta |
| `T_SN_OFERTA_SLOT` | — | Slots de horário por oferta |
| `T_SN_MENSAGEM_SITE` | — | Formulário de contato público |

### Executar DDL

```bash
# SQL Developer ou SQL*Plus
@scriptatualizado.sql
```

---

## 🧪 Testes

```bash
# Executar todos os testes
mvnw.cmd test          # Windows
./mvnw test            # Linux/Mac

# Testes disponíveis:
# - PacienteTest.java          → calcularUrgencia, verificarElegibilidade
# - DentistaTest.java          → temVagaDisponivel
# - PacienteSoftDeleteTest.java → soft delete e reativação
```

---

## 🚢 Build e Deploy

### Build para produção

```bash
mvnw.cmd package -DskipTests     # Windows
./mvnw package -DskipTests       # Linux/Mac

# Executar o JAR
java -jar target/quarkus-app/quarkus-run.jar
```

### Docker

```bash
docker build -f Dockerfile -t dentista-na-nuvem-api .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL \
  -e DB_USER=RM567980 \
  -e DB_PASSWORD=senha \
  dentista-na-nuvem-api
```

### Azure Container Apps

Deploy automático via GitHub Actions em `.github/workflows/` a cada push na branch `master`.

---

## 🔗 Repositórios Relacionados

| Repositório | Descrição |
|-------------|-----------|
| [Challenge-backend](https://github.com/Kayque2012/Challenge-backend) | Este repositório — API Java |
| [Challenge-Sprint](https://github.com/Kayque2012/Challenge-Sprint) | Front-end React + Vite |

**Front-end em produção:** https://challenge-sprint-rose.vercel.app

---

## 📄 Licença

Projeto acadêmico — **FIAP Challenge 2025**  
ONG parceira: [Turma do Bem](https://www.turmadabem.org.br)

---

<div align="center">
  Desenvolvido pela turma <strong>1TDSPB</strong> — FIAP 2025
</div>
