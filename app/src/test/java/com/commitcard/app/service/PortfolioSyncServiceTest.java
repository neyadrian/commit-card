package com.commitcard.app.service;

import com.commitcard.app.dto.GithubRepoDTO;
import com.commitcard.app.dto.GithubUserDTO;
import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.ProjectRepository;
import com.commitcard.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Testes unitários (sem subir o Spring inteiro — bem mais rápido).
// O GithubService, UserRepository e ProjectRepository são "fingidos" (mocks),
// então a gente controla exatamente o que eles devolvem e testa só a lógica
// de verdade que está dentro do PortfolioSyncService.
@ExtendWith(MockitoExtension.class)
class PortfolioSyncServiceTest {

    @Mock
    private GithubService githubService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private PortfolioSyncService portfolioSyncService;

    private GithubUserDTO criarGithubUserDTO(String login, String nome) {
        GithubUserDTO dto = new GithubUserDTO();
        dto.setLogin(login);
        dto.setNome(nome);
        dto.setBio("Bio de teste");
        dto.setAvatarUrl("https://avatars.example.com/" + login);
        return dto;
    }

    private GithubRepoDTO criarGithubRepoDTO(String nome, boolean fork) {
        GithubRepoDTO dto = new GithubRepoDTO();
        dto.setNome(nome);
        dto.setDescricao("Descrição de " + nome);
        dto.setHtmlUrl("https://github.com/teste/" + nome);
        dto.setStargazersCount(10);
        dto.setForks(2);
        dto.setLanguage("Java");
        dto.setFork(fork);
        return dto;
    }

    @Test
    void deveCriarNovoUsuarioQuandoAindaNaoExisteNoBanco() {
        when(githubService.buscarUsuario("neyadrian")).thenReturn(criarGithubUserDTO("neyadrian", "Ney Adrian"));
        when(githubService.buscarRepositorios("neyadrian")).thenReturn(List.of());
        when(userRepository.findByGithubUsername("neyadrian")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(chamada -> chamada.getArgument(0));

        User resultado = portfolioSyncService.sincronizar("neyadrian");

        assertThat(resultado.getGithubUsername()).isEqualTo("neyadrian");
        assertThat(resultado.getNome()).isEqualTo("Ney Adrian");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveAtualizarUsuarioExistenteEmVezDeCriarDuplicado() {
        User usuarioExistente = new User("neyadrian");
        usuarioExistente.setNome("Nome Antigo");

        when(githubService.buscarUsuario("neyadrian")).thenReturn(criarGithubUserDTO("neyadrian", "Nome Novo"));
        when(githubService.buscarRepositorios("neyadrian")).thenReturn(List.of());
        when(userRepository.findByGithubUsername("neyadrian")).thenReturn(Optional.of(usuarioExistente));
        when(userRepository.save(any(User.class))).thenAnswer(chamada -> chamada.getArgument(0));

        User resultado = portfolioSyncService.sincronizar("neyadrian");

        // é o MESMO objeto atualizado, não um novo criado do zero
        assertThat(resultado).isSameAs(usuarioExistente);
        assertThat(resultado.getNome()).isEqualTo("Nome Novo");
    }

    @Test
    void deveIgnorarRepositoriosQueSaoFork() {
        User usuario = new User("neyadrian");

        when(githubService.buscarUsuario("neyadrian")).thenReturn(criarGithubUserDTO("neyadrian", "Ney Adrian"));
        when(githubService.buscarRepositorios("neyadrian")).thenReturn(List.of(
                criarGithubRepoDTO("projeto-original", false),
                criarGithubRepoDTO("projeto-fork", true)
        ));
        when(userRepository.findByGithubUsername("neyadrian")).thenReturn(Optional.of(usuario));
        when(userRepository.save(any(User.class))).thenAnswer(chamada -> chamada.getArgument(0));

        User resultado = portfolioSyncService.sincronizar("neyadrian");

        assertThat(resultado.getProjetos()).hasSize(1);
        assertThat(resultado.getProjetos().get(0).getNomeRepositorio()).isEqualTo("projeto-original");
    }

    @Test
    void deveSubstituirProjetosAntigosPelosAtuaisNumaResincronizacao() {
        User usuario = new User("neyadrian");
        Project projetoQueNaoExisteMaisNoGithub = new Project();
        projetoQueNaoExisteMaisNoGithub.setNomeRepositorio("projeto-deletado");
        projetoQueNaoExisteMaisNoGithub.setUser(usuario);
        usuario.getProjetos().add(projetoQueNaoExisteMaisNoGithub);

        when(githubService.buscarUsuario("neyadrian")).thenReturn(criarGithubUserDTO("neyadrian", "Ney Adrian"));
        when(githubService.buscarRepositorios("neyadrian")).thenReturn(List.of(
                criarGithubRepoDTO("projeto-atual", false)
        ));
        when(userRepository.findByGithubUsername("neyadrian")).thenReturn(Optional.of(usuario));
        when(userRepository.save(any(User.class))).thenAnswer(chamada -> chamada.getArgument(0));

        User resultado = portfolioSyncService.sincronizar("neyadrian");

        assertThat(resultado.getProjetos()).hasSize(1);
        assertThat(resultado.getProjetos().get(0).getNomeRepositorio()).isEqualTo("projeto-atual");
    }
}