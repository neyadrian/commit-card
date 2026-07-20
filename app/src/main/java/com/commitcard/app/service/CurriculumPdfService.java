package com.commitcard.app.service;

import com.commitcard.app.dto.CurriculoRequestDTO;
import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Base64;
import java.util.List;

// IMPORTANTE: os getters usados aqui (getNomeRepositorio, getDescricao, getUrlRepositorio,
// getLinguagemPrincipal, isDestaque) seguem os nomes de campo que apareceram no seu H2/JSON.
// Se sua entidade Project usar nomes diferentes, ajusta os getters correspondentes abaixo.
@Service
public class CurriculumPdfService {

    private static final Color COR_TEXTO = new Color(28, 30, 36);
    private static final Color COR_TEXTO_SUAVE = new Color(102, 107, 117);
    private static final Color COR_ACCENT = new Color(5, 150, 105);
    private static final Color COR_LINHA = new Color(226, 229, 234);
    private static final float TAMANHO_FOTO = 70f;

    /**
     * Versão simples: usa só os dados sincronizados do GitHub.
     * Mantida para o botão de "exportar rápido", sem precisar preencher formulário.
     */
    public byte[] gerarPdf(User user) {
        Document document = new Document(PageSize.A4, 48, 48, 50, 50);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font fonteNome = new Font(Font.HELVETICA, 22, Font.BOLD, COR_TEXTO);
            document.add(new Paragraph(user.getNome(), fonteNome));

            Font fonteContato = new Font(Font.HELVETICA, 10, Font.NORMAL, COR_TEXTO_SUAVE);
            Paragraph contato = new Paragraph("github.com/" + user.getGithubUsername(), fonteContato);
            contato.setSpacingBefore(4);
            document.add(contato);

            if (user.getBio() != null && !user.getBio().isBlank()) {
                Font fonteBio = new Font(Font.HELVETICA, 11, Font.NORMAL, COR_TEXTO_SUAVE);
                Paragraph bio = new Paragraph(user.getBio(), fonteBio);
                bio.setSpacingBefore(8);
                document.add(bio);
            }

            document.add(linhaDivisoria());
            adicionarTituloSecao(document, "PROJETOS EM DESTAQUE");
            adicionarSecaoProjetos(document, user.getProjetos());

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

        return outputStream.toByteArray();
    }

    /**
     * Versão completa: combina os dados preenchidos no formulário (contato, resumo,
     * experiências, formação, habilidades, foto) com os projetos em destaque do GitHub.
     */
    public byte[] gerarPdfCompleto(User user, CurriculoRequestDTO dados) {
        Document document = new Document(PageSize.A4, 48, 48, 50, 50);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            String nomeExibido = temTexto(dados != null ? dados.getNomeCompleto() : null)
                    ? dados.getNomeCompleto()
                    : user.getNome();

            String contato = montarLinhaContato(dados, user);
            Image foto = obterImagemPerfil(dados, user);

            adicionarCabecalho(document, nomeExibido, contato, foto);
            document.add(linhaDivisoria());

            if (dados != null && temTexto(dados.getResumo())) {
                adicionarTituloSecao(document, "RESUMO");
                Font fonteResumo = new Font(Font.HELVETICA, 10.5f, Font.NORMAL, COR_TEXTO);
                Paragraph resumo = new Paragraph(dados.getResumo(), fonteResumo);
                resumo.setSpacingAfter(6);
                document.add(resumo);
            }

            if (dados != null && dados.getExperiencias() != null && !dados.getExperiencias().isEmpty()) {
                adicionarTituloSecao(document, "EXPERIÊNCIA");
                for (CurriculoRequestDTO.ExperienciaDTO exp : dados.getExperiencias()) {
                    adicionarExperiencia(document, exp);
                }
            }

            if (dados != null && dados.getFormacoes() != null && !dados.getFormacoes().isEmpty()) {
                adicionarTituloSecao(document, "FORMAÇÃO");
                for (CurriculoRequestDTO.FormacaoDTO formacao : dados.getFormacoes()) {
                    adicionarFormacao(document, formacao);
                }
            }

            if (dados != null && dados.getHabilidades() != null && !dados.getHabilidades().isEmpty()) {
                adicionarTituloSecao(document, "HABILIDADES");
                String habilidadesTexto = String.join("   ·   ", dados.getHabilidades());
                Font fonteHabilidades = new Font(Font.HELVETICA, 10.5f, Font.NORMAL, COR_TEXTO);
                Paragraph habilidades = new Paragraph(habilidadesTexto, fonteHabilidades);
                habilidades.setSpacingAfter(6);
                document.add(habilidades);
            }

            adicionarTituloSecao(document, "PROJETOS EM DESTAQUE (GITHUB)");
            adicionarSecaoProjetos(document, user.getProjetos());

            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

        return outputStream.toByteArray();
    }

    /**
     * Monta o cabeçalho do currículo. Se tiver foto, usa uma tabela de 2 colunas
     * (foto | nome+contato) pra ficarem lado a lado; sem foto, é só texto normal.
     */
    private void adicionarCabecalho(Document document, String nomeExibido, String contato, Image foto) throws DocumentException {
        Font fonteNome = new Font(Font.HELVETICA, 24, Font.BOLD, COR_TEXTO);
        Font fonteContato = new Font(Font.HELVETICA, 10, Font.NORMAL, COR_TEXTO_SUAVE);

        if (foto == null) {
            document.add(new Paragraph(nomeExibido, fonteNome));
            if (!contato.isBlank()) {
                Paragraph linhaContato = new Paragraph(contato, fonteContato);
                linhaContato.setSpacingBefore(4);
                document.add(linhaContato);
            }
            return;
        }

        foto.scaleToFit(TAMANHO_FOTO, TAMANHO_FOTO);

        PdfPTable tabelaCabecalho = new PdfPTable(2);
        tabelaCabecalho.setWidthPercentage(100);
        tabelaCabecalho.setWidths(new float[]{1f, 4.2f});

        PdfPCell celulaFoto = new PdfPCell(foto, false);
        celulaFoto.setBorder(Rectangle.NO_BORDER);
        celulaFoto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celulaFoto.setPadding(0);
        celulaFoto.setPaddingRight(14);
        tabelaCabecalho.addCell(celulaFoto);

        PdfPCell celulaTexto = new PdfPCell();
        celulaTexto.setBorder(Rectangle.NO_BORDER);
        celulaTexto.setVerticalAlignment(Element.ALIGN_MIDDLE);
        celulaTexto.setPadding(0);
        celulaTexto.addElement(new Paragraph(nomeExibido, fonteNome));
        if (!contato.isBlank()) {
            Paragraph linhaContato = new Paragraph(contato, fonteContato);
            linhaContato.setSpacingBefore(4);
            celulaTexto.addElement(linhaContato);
        }
        tabelaCabecalho.addCell(celulaTexto);

        document.add(tabelaCabecalho);
    }

    /**
     * Decide de onde vem a foto: um upload em base64 tem prioridade; senão,
     * se o usuário marcou pra usar a do GitHub, busca pela URL do avatar.
     * Se qualquer coisa falhar (imagem inválida, URL fora do ar), retorna null
     * e o PDF é gerado normalmente, só sem foto.
     */
    private Image obterImagemPerfil(CurriculoRequestDTO dados, User user) {
        if (dados == null) return null;

        try {
            if (temTexto(dados.getFotoBase64())) {
                String base64 = dados.getFotoBase64();
                int virgula = base64.indexOf(',');
                String dadosPuros = virgula >= 0 ? base64.substring(virgula + 1) : base64;
                byte[] bytes = Base64.getDecoder().decode(dadosPuros);
                return Image.getInstance(bytes);
            }

            if (dados.isUsarFotoGithub() && temTexto(user.getAvatarUrl())) {
                return Image.getInstance(new URL(user.getAvatarUrl()));
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    private boolean temTexto(String valor) {
        return valor != null && !valor.isBlank();
    }

    private String montarLinhaContato(CurriculoRequestDTO dados, User user) {
        StringBuilder sb = new StringBuilder();
        if (dados != null) {
            adicionarSeparadoSePresente(sb, dados.getEmail());
            adicionarSeparadoSePresente(sb, dados.getTelefone());
            adicionarSeparadoSePresente(sb, dados.getLocalizacao());
        }
        adicionarSeparadoSePresente(sb, "github.com/" + user.getGithubUsername());
        return sb.toString();
    }

    private void adicionarSeparadoSePresente(StringBuilder sb, String valor) {
        if (valor == null || valor.isBlank()) return;
        if (sb.length() > 0) sb.append("   ·   ");
        sb.append(valor.trim());
    }

    private void adicionarTituloSecao(Document document, String titulo) throws DocumentException {
        Font fonteTitulo = new Font(Font.HELVETICA, 11, Font.BOLD, COR_ACCENT);
        Paragraph paragrafo = new Paragraph(titulo, fonteTitulo);
        paragrafo.setSpacingBefore(14);
        paragrafo.setSpacingAfter(6);
        document.add(paragrafo);
    }

    private Paragraph linhaDivisoria() {
        LineSeparator linha = new LineSeparator(0.6f, 100, COR_LINHA, Element.ALIGN_LEFT, 0);
        Paragraph paragrafo = new Paragraph();
        paragrafo.setSpacingBefore(8);
        paragrafo.add(linha);
        return paragrafo;
    }

    private void adicionarExperiencia(Document document, CurriculoRequestDTO.ExperienciaDTO exp) throws DocumentException {
        if (!temTexto(exp.getCargo()) && !temTexto(exp.getEmpresa())) return;

        Font fonteCargo = new Font(Font.HELVETICA, 12, Font.BOLD, COR_TEXTO);
        Paragraph titulo = new Paragraph(juntarComTraco(exp.getCargo(), exp.getEmpresa()), fonteCargo);
        titulo.setSpacingBefore(6);
        document.add(titulo);

        if (temTexto(exp.getPeriodo())) {
            Font fontePeriodo = new Font(Font.HELVETICA, 9.5f, Font.ITALIC, COR_TEXTO_SUAVE);
            document.add(new Paragraph(exp.getPeriodo(), fontePeriodo));
        }

        if (temTexto(exp.getDescricao())) {
            Font fonteDescricao = new Font(Font.HELVETICA, 10, Font.NORMAL, COR_TEXTO);
            Paragraph descricao = new Paragraph(exp.getDescricao(), fonteDescricao);
            descricao.setSpacingBefore(3);
            document.add(descricao);
        }
    }

    private void adicionarFormacao(Document document, CurriculoRequestDTO.FormacaoDTO formacao) throws DocumentException {
        if (!temTexto(formacao.getCurso()) && !temTexto(formacao.getInstituicao())) return;

        Font fonteCurso = new Font(Font.HELVETICA, 12, Font.BOLD, COR_TEXTO);
        Paragraph titulo = new Paragraph(juntarComTraco(formacao.getCurso(), formacao.getInstituicao()), fonteCurso);
        titulo.setSpacingBefore(6);
        document.add(titulo);

        if (temTexto(formacao.getPeriodo())) {
            Font fontePeriodo = new Font(Font.HELVETICA, 9.5f, Font.ITALIC, COR_TEXTO_SUAVE);
            document.add(new Paragraph(formacao.getPeriodo(), fontePeriodo));
        }
    }

    private String juntarComTraco(String a, String b) {
        if (temTexto(a) && temTexto(b)) return a + " — " + b;
        if (temTexto(a)) return a;
        if (temTexto(b)) return b;
        return "";
    }

    private void adicionarSecaoProjetos(Document document, List<Project> projetos) throws DocumentException {
        if (projetos == null) return;

        for (Project projeto : projetos) {
            if (!projeto.isDestaque()) continue;

            Font fonteProjeto = new Font(Font.HELVETICA, 12, Font.BOLD, COR_TEXTO);
            Paragraph nomeProjeto = new Paragraph(projeto.getNomeRepositorio(), fonteProjeto);
            nomeProjeto.setSpacingBefore(6);
            document.add(nomeProjeto);

            if (temTexto(projeto.getDescricao())) {
                Font fonteDescricao = new Font(Font.HELVETICA, 10, Font.NORMAL, COR_TEXTO_SUAVE);
                document.add(new Paragraph(projeto.getDescricao(), fonteDescricao));
            }

            Font fonteMeta = new Font(Font.HELVETICA, 9.5f, Font.NORMAL, COR_ACCENT);
            String meta = (temTexto(projeto.getLinguagemPrincipal()) ? projeto.getLinguagemPrincipal() + "   ·   " : "")
                    + "★ " + projeto.getStars() + "   ·   " + projeto.getForks() + " forks";
            Paragraph paragrafoMeta = new Paragraph(meta, fonteMeta);
            paragrafoMeta.setSpacingBefore(2);
            document.add(paragrafoMeta);
        }
    }
}