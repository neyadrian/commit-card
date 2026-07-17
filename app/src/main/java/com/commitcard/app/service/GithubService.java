package com.commitcard.app.service;

import com.commitcard.app.dto.GithubRepoDTO;
import com.commitcard.app.dto.GithubUserDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class GithubService {

    private static final String GITHUB_API =  "https://api.github.com";
    private final RestTemplate restTemplate;

    public GithubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GithubUserDTO buscarUsuario(String username) {
        String url = GITHUB_API + "/users/" + username;
        return restTemplate.getForObject(url, GithubUserDTO.class);
    }

    public List<GithubRepoDTO> buscarRepositorios(String username) {
        String url = GITHUB_API + "/users/" + username + "/repos";
        GithubRepoDTO[] repos = restTemplate.getForObject(url, GithubRepoDTO[].class);

        if (repos == null) {
            return List.of();
        } else {
            return Arrays.asList(repos);
        }
    }
}
