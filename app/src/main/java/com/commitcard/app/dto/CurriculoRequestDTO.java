package com.commitcard.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// Recebe os dados que o usuário preenche no formulário de currículo,
// pra combinar com os projetos sincronizados do GitHub na hora de gerar o PDF.
@Getter
@Setter
@NoArgsConstructor
public class CurriculoRequestDTO {

    private String nomeCompleto;
    private String email;
    private String telefone;
    private String localizacao;
    private String resumo;
    private List<ExperienciaDTO> experiencias;
    private List<FormacaoDTO> formacoes;
    private List<String> habilidades;

    // Foto de perfil: ou usa a mesma do GitHub, ou usa uma enviada pelo usuário (base64).
    // Se usarFotoGithub for true, fotoBase64 é ignorado.
    private boolean usarFotoGithub;
    private String fotoBase64;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ExperienciaDTO {
        private String cargo;
        private String empresa;
        private String periodo;
        private String descricao;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class FormacaoDTO {
        private String curso;
        private String instituicao;
        private String periodo;
    }
}