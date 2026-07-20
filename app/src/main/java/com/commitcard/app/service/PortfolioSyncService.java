package com.commitcard.app.service;

import com.commitcard.app.dto.GithubRepoDTO;
import com.commitcard.app.dto.GithubUserDTO;
import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.ProjectRepository;
import com.commitcard.app.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PortfolioSyncService {

    private final GithubService githubService;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    public PortfolioSyncService(GithubService githubService, UserRepository userRepository, ProjectRepository projectRepository) {
        this.githubService = githubService;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    @Transactional
    public User sincronizar(String githubUsername) {
        // Busca os dados mais recentes do github
        GithubUserDTO githubUser = githubService.buscarUsuario(githubUsername);
        List<GithubRepoDTO> repos = githubService.buscarRepositorios(githubUsername);

        // Encontra o usuário no banco de dados ou cria um novo
        User user = userRepository.findByGithubUsername(githubUsername).orElseGet(() -> new User(githubUsername));

        // Atualiza os dados do usuário
        user.setNome(githubUser.getNome());
        user.setBio(githubUser.getBio());
        user.setAvatarUrl(githubUser.getAvatarUrl());

        // Limpa a lista de projetos antigos
        user.getProjetos().clear();

        // Itera sobre os repositórios do GitHub, filtra os que não são forks e cria os novos projetos
        repos.stream()
                .filter(repo -> !repo.getFork())
                .forEach(repoDto -> {
                    // Cria a nova entidade Project
                    Project novoProjeto = new Project(
                            repoDto.getNome(),
                            repoDto.getHtmlUrl(),
                            user
                    );
                    // Preenche os outros campos com os dados do DTO
                    novoProjeto.setDescricao(repoDto.getDescricao());
                    novoProjeto.setLinguagemPrincipal(repoDto.getLanguage());
                    novoProjeto.setStars(repoDto.getStargazersCount());
                    novoProjeto.setForks(repoDto.getForks());

                    // Adiciona o projeto à lista do usuário
                    user.getProjetos().add(novoProjeto);
                });
        // Salva o usuário. O CascadeType.ALL se encarrega de salvar os novos projetos.
        return userRepository.save(user);
    }
}
