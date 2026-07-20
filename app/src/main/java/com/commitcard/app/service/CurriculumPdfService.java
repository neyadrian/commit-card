package com.commitcard.app.service;

import com.commitcard.app.model.User;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class CurriculumPdfService {

    public byte[] gerarPdf(User user) {
        Document document = new Document();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Fonte para o nome (título grande, negrito)
            Font fonteNome = new Font(Font.HELVETICA, 22, Font.BOLD);
            Paragraph nome = new Paragraph(user.getNome(), fonteNome);
            document.add(nome);

            // Fonte para a bio (texto normal, menor)
            if (user.getBio() != null) {
                Font fonteBio = new Font(Font.HELVETICA, 12, Font.NORMAL);
                Paragraph bio = new Paragraph(user.getBio(), fonteBio);
                document.add(bio);
            }

            document.add(Chunk.NEWLINE); // espaço em branco

            // Seção de projetos em destaque
            user.getProjetos().stream()
                    .filter(projeto -> projeto.isDestaque())
                    .forEach(projeto -> {
                        try {
                            Font fonteProjeto = new Font(Font.HELVETICA, 14, Font.BOLD);
                            Paragraph nomeProjeto = new Paragraph(projeto.getNomeRepositorio(), fonteProjeto);
                            document.add(nomeProjeto);

                            Font fonteDescricao = new Font(Font.HELVETICA, 11, Font.NORMAL);
                            String textoDescricao = projeto.getDescricao() + " — " + projeto.getLinguagemPrincipal() + " · " + projeto.getStars() + " stars";
                            Paragraph descricaoProjeto = new Paragraph(textoDescricao, fonteDescricao);
                            document.add(descricaoProjeto);

                            document.add(Chunk.NEWLINE);
                        } catch (DocumentException e) {
                            throw new RuntimeException("Erro ao adicionar projeto no PDF", e);
                        }
                    });

            document.close(); // fecha só depois de adicionar TUDO

        } catch (DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }

        return outputStream.toByteArray();
    }
}