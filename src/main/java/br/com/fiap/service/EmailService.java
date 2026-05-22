package br.com.fiap.service;

import br.com.fiap.dto.EmailLembreteDTO;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    // ─────────────────────────────────────────────────────────────────────────
    // E-mail de CONFIRMAÇÃO (enviado imediatamente ao paciente confirmar slot)
    // ─────────────────────────────────────────────────────────────────────────
    public void enviarConfirmacao(EmailLembreteDTO dto) {
        String assunto = "✅ Consulta confirmada — Turma do Bem";
        String corpo   = htmlConfirmacao(dto);
        mailer.send(Mail.withHtml(dto.para, assunto, corpo));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // E-mail de LEMBRETE (D-3, D-2, D-1, D-0)
    // ─────────────────────────────────────────────────────────────────────────
    public void enviarLembrete(EmailLembreteDTO dto) {
        String diasTexto = dto.diasAntes == 0
                ? "🗓️ Hoje é o dia da sua consulta!"
                : "⏰ Faltam apenas " + dto.diasAntes + " dia(s) para a sua consulta!";

        String assunto = "🔔 Lembrete: consulta em " + dto.diasAntes + " dia(s) — Turma do Bem";
        String corpo   = htmlLembrete(dto, diasTexto);
        mailer.send(Mail.withHtml(dto.para, assunto, corpo));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Templates HTML
    // ─────────────────────────────────────────────────────────────────────────
    private String htmlConfirmacao(EmailLembreteDTO d) {
        return baseHtml(
            "✅ Consulta Confirmada!",
            "Olá, <strong>" + d.nome + "</strong>!",
            "Sua consulta foi confirmada com sucesso. Veja os detalhes abaixo:",
            d, "#8dc63f"
        );
    }

    private String htmlLembrete(EmailLembreteDTO d, String diasTexto) {
        return baseHtml(
            "🔔 Lembrete de Consulta",
            diasTexto,
            "Olá, <strong>" + d.nome + "</strong>! Não se esqueça da sua consulta:",
            d, "#FF8C00"
        );
    }

    private String baseHtml(String titulo, String cabecalho, String intro,
                             EmailLembreteDTO d, String cor) {
        return """
            <!DOCTYPE html>
            <html lang="pt-BR">
            <head><meta charset="UTF-8">
            <style>
              body { font-family: Arial, sans-serif; background:#f5f5dc; margin:0; padding:20px; }
              .card { background:#fff; border-radius:16px; padding:32px; max-width:520px;
                      margin:0 auto; box-shadow:0 2px 12px rgba(0,0,0,.08); }
              .badge { background:%s; color:#fff; border-radius:8px; padding:12px 20px;
                       display:inline-block; margin:16px 0; font-size:15px; font-weight:bold; }
              .row { display:flex; gap:8px; align-items:center; margin:8px 0; color:#555; }
              .label { font-size:11px; font-weight:bold; color:#999; text-transform:uppercase; }
              .footer { text-align:center; font-size:11px; color:#aaa; margin-top:24px; }
            </style></head>
            <body>
            <div class="card">
              <p style="font-size:22px; font-weight:900; color:#333; margin:0 0 4px">%s</p>
              <p style="color:%s; font-weight:bold; margin:0 0 16px">%s</p>
              <p style="color:#555">%s</p>
              <div class="badge" style="background:%s">
                📅 &nbsp;%s &nbsp;·&nbsp; %s
              </div>
              <div class="row"><span class="label">Procedimento</span>&nbsp;%s</div>
              <div class="row"><span class="label">Dentista</span>&nbsp;Dr(a). %s</div>
              <hr style="border:none;border-top:1px solid #eee;margin:24px 0">
              <p style="color:#555; font-size:13px">
                Em caso de dúvidas ou necessidade de remarcar, entre em contato com o seu dentista voluntário.
              </p>
              <div class="footer">
                Turma do Bem — Odontologia voluntária para jovens em vulnerabilidade social.<br>
                Este é um e-mail automático, não responda.
              </div>
            </div>
            </body></html>
            """.formatted(cor, titulo, cor, cabecalho, intro, cor, d.data, d.hora,
                          d.procedimento, d.dentista);
    }
}
