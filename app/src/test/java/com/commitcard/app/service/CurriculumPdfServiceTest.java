package com.commitcard.app.service;

import com.commitcard.app.dto.CurriculoRequestDTO;
import com.commitcard.app.model.Certificado;
import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// Não precisa de mock nem de Spring aqui — o serviço não tem dependências externas,
// só monta o PDF a partir dos objetos que a gente passa. O teste mais valioso aqui
// é simplesmente confirmar que o método NÃO explode e devolve um PDF válido de verdade,
// não um monte de bytes qualquer.
class CurriculumPdfServiceTest {

    private final CurriculumPdfService service = new CurriculumPdfService();

    private User criarUsuarioDeTeste() {
        User user = new User("neyadrian");
        user.setNome("Ney Adrian");
        user.setBio("Estudante de Engenharia da Computação");
        user.setAvatarUrl("https://avatars.example.com/neyadrian");

        Project projeto = new Project();
        projeto.setNomeRepositorio("commitcard");
        projeto.setDescricao("Gerador de portfólio a partir do GitHub");
        projeto.setLinguagemPrincipal("Java");
        projeto.setStars(5);
        projeto.setForks(1);
        projeto.setDestaque(true);
        projeto.setUser(user);
        user.getProjetos().add(projeto);

        Certificado certificado = new Certificado();
        certificado.setTitulo("Certificação de teste");
        certificado.setInstituicao("Instituição de teste");
        certificado.setData("2026");
        certificado.setUser(user);
        user.getCertificados().add(certificado);

        return user;
    }

    @Test
    void deveGerarPdfSimplesComBytesValidos() {
        User user = criarUsuarioDeTeste();

        byte[] pdf = service.gerarPdf(user);

        assertThat(pdf).isNotEmpty();
        assertThat(comecaComAssinaturaDePdf(pdf)).isTrue();
    }

    @Test
    void deveGerarPdfCompletoMesmoSemDadosDoFormulario() {
        User user = criarUsuarioDeTeste();

        byte[] pdf = service.gerarPdfCompleto(user, null);

        assertThat(pdf).isNotEmpty();
        assertThat(comecaComAssinaturaDePdf(pdf)).isTrue();
    }

    @Test
    void deveGerarPdfCompletoComDadosDoFormularioPreenchidos() {
        User user = criarUsuarioDeTeste();

        CurriculoRequestDTO dados = new CurriculoRequestDTO();
        dados.setNomeCompleto("Ney Adrian Casimiro Oliveira");
        dados.setEmail("teste@example.com");
        dados.setResumo("Resumo profissional de teste");

        byte[] pdf = service.gerarPdfCompleto(user, dados);

        assertThat(pdf).isNotEmpty();
        assertThat(comecaComAssinaturaDePdf(pdf)).isTrue();
    }

    // Todo PDF de verdade começa com "%PDF" nos primeiros 4 bytes do arquivo —
    // é a "assinatura" que confirma que não geramos um arquivo corrompido/vazio.
    private boolean comecaComAssinaturaDePdf(byte[] pdf) {
        if (pdf.length < 4) return false;
        return pdf[0] == '%' && pdf[1] == 'P' && pdf[2] == 'D' && pdf[3] == 'F';
    }
}