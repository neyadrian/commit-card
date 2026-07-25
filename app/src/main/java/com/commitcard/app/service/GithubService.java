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

import java.util.ArrayList;
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

    // Busca TODOS os repositórios, não só os primeiros 30 — o GitHub limita cada
    // "página" da API a no máximo 100 resultados, então repositórios acima disso
    // (100, 200, etc.) exigem pedir a página seguinte, uma de cada vez, até o
    // GitHub devolver uma página vazia (sinal de que já chegamos no final).
    public List<GithubRepoDTO> buscarRepositorios(String username) {
        List<GithubRepoDTO> todos = new ArrayList<>();
        int pagina = 1;
        int maximoDePaginas = 10; // trava de segurança: 10 x 100 = até 1000 repositórios

        while (pagina <= maximoDePaginas) {
            String url = GITHUB_API + "/users/" + username + "/repos?per_page=100&page=" + pagina;
            ResponseEntity<GithubRepoDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, criarRequestComHeaders(), GithubRepoDTO[].class
            );
            GithubRepoDTO[] repos = response.getBody();

            if (repos == null || repos.length == 0) {
                break;
            }

            todos.addAll(Arrays.asList(repos));

            // Página veio com menos de 100 = era a última página, não precisa pedir mais.
            if (repos.length < 100) {
                break;
            }

            pagina++;
        }

        return todos;
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