package com.commitcard.app.service;

import com.commitcard.app.dto.GithubRepoDTO;
import com.commitcard.app.dto.GithubUserDTO;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioSyncService {

    private final GithubService githubService;
    private final UserRepository userRepository;

    public PortfolioSyncService(GithubService githubService, UserRepository userRepository) {
        this.githubService = githubService;
        this.userRepository = userRepository;
    }

    public User sincronizar(String githubUsername) {
        GithubUserDTO githubUser = githubService.buscarUsuario(githubUsername);
        List<GithubRepoDTO> repos = githubService.buscarRepositorios(githubUsername);

        User user = userRepository.findByGithubUsername(githubUsername).orElseGet(() -> new User(githubUsername));

        user.setNome(githubUser.getNome());
        user.setBio(githubUser.getBio());
        user.setAvatarUrl(githubUser.getAvatarUrl());

        return userRepository.save(user);
    }
}
