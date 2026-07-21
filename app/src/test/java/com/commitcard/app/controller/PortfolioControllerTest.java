package com.commitcard.app.controller;

import com.commitcard.app.config.SecurityConfig;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.CertificadoRepository;
import com.commitcard.app.repository.ProjectRepository;
import com.commitcard.app.repository.UserRepository;
import com.commitcard.app.security.OAuthSuccessHandler;
import com.commitcard.app.service.CurriculumPdfService;
import com.commitcard.app.service.PortfolioSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest sobe só a camada web (controller + segurança), sem tocar em banco de
// verdade — os serviços e repositórios usados pelo controller são "fingidos" (@MockitoBean).
// O @TestPropertySource dá um client-id/secret falso só pra esse teste: sem eles, o Spring
// tenta validar a configuração real do OAuth2 e falha por não achar as variáveis de ambiente
// (que só existem na configuração do IntelliJ, não no terminal puro). Como os testes usam
// oauth2Login() pra simular o login, nunca acontece uma chamada de verdade pro GitHub.
// O @Import(SecurityConfig.class) garante que a NOSSA regra de segurança (não uma genérica
// do Spring) seja usada nesse recorte de teste — sem isso, requisições sem login podem cair
// numa configuração padrão diferente da que está valendo de verdade na aplicação.
@WebMvcTest(PortfolioController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "spring.security.oauth2.client.registration.github.client-id=id-de-teste",
        "spring.security.oauth2.client.registration.github.client-secret=segredo-de-teste"
})
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PortfolioSyncService syncService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private CurriculumPdfService curriculumPdfService;

    @MockitoBean
    private ProjectRepository projectRepository;

    @MockitoBean
    private CertificadoRepository certificadoRepository;

    // O SecurityConfig depende do OAuthSuccessHandler pra construir o filtro de login;
    // como esse slice de teste não sobe os @Component normais, precisa fingir esse também.
    @MockitoBean
    private OAuthSuccessHandler oAuthSuccessHandler;

    @Test
    void verPortfolioNaoExigeLogin() throws Exception {
        User user = new User("neyadrian");
        when(userRepository.findByGithubUsername("neyadrian")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/portfolio/neyadrian"))
                .andExpect(status().isOk());
    }

    @Test
    void verPortfolioRetorna404QuandoNaoEncontrado() throws Exception {
        when(userRepository.findByGithubUsername("ninguem")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/portfolio/ninguem"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sincronizarSemEstarLogadoNaoEhPermitido() throws Exception {
        // Sem estar logado, o fluxo de OAuth2 redireciona pra página de login do GitHub
        // (status 3xx) em vez de devolver um 401/403 direto — por isso o teste checa
        // "não é 200", não um código de erro específico.
        mockMvc.perform(post("/api/portfolio/sync/neyadrian").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void sincronizarComOutroUsuarioLogadoRetorna403() throws Exception {
        mockMvc.perform(post("/api/portfolio/sync/neyadrian")
                        .with(oauth2Login().attributes(atributos -> atributos.put("login", "outra-pessoa")))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void sincronizarComOProprioUsuarioLogadoFunciona() throws Exception {
        when(syncService.sincronizar("neyadrian")).thenReturn(new User("neyadrian"));

        mockMvc.perform(post("/api/portfolio/sync/neyadrian")
                        .with(oauth2Login().attributes(atributos -> atributos.put("login", "neyadrian")))
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}