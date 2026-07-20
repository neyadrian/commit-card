package com.commitcard.app.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

// Expõe pro front se a pessoa está logada, e com qual usuário do GitHub.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> resposta = new LinkedHashMap<>();

        if (principal == null) {
            resposta.put("autenticado", false);
            return ResponseEntity.ok(resposta);
        }

        String login = principal.getAttribute("login");
        String nome = principal.getAttribute("name");
        String avatarUrl = principal.getAttribute("avatar_url");

        resposta.put("autenticado", true);
        resposta.put("githubUsername", login);
        resposta.put("nome", nome != null ? nome : login);
        resposta.put("avatarUrl", avatarUrl);

        return ResponseEntity.ok(resposta);
    }
}