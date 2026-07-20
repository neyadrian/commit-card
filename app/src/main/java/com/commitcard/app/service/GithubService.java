package com.commitcard.app.service;

import com.commitcard.app.dto.GithubRepoDTO;
import com.commitcard.app.dto.GithubUserDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Service
public class GithubService {

    private static final String GITHUB_API = "https://api.github.com";
    private final RestTemplate restTemplate;

    @Value("${github.api.token:}")
    private String githubToken;

    public GithubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public GithubUserDTO buscarUsuario(String username) {
        String url = GITHUB_API + "/users/" + username;
        ResponseEntity<GithubUserDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, criarRequestComHeaders(), GithubUserDTO.class
        );
        return response.getBody();
    }

    public List<GithubRepoDTO> buscarRepositorios(String username) {
        String url = GITHUB_API + "/users/" + username + "/repos";
        ResponseEntity<GithubRepoDTO[]> response = restTemplate.exchange(
                url, HttpMethod.GET, criarRequestComHeaders(), GithubRepoDTO[].class
        );
        GithubRepoDTO[] repos = response.getBody();
        return repos == null ? List.of() : Arrays.asList(repos);
    }

    private HttpEntity<Void> criarRequestComHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github+json");
        if (githubToken != null && !githubToken.isBlank()) {
            headers.set("Authorization", "Bearer " + githubToken);
        }
        return new HttpEntity<>(headers);
    }
}