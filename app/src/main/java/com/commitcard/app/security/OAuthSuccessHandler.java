package com.commitcard.app.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Depois de logar com o GitHub, manda a pessoa direto pro portfólio dela
// (usando o "login" do GitHub, que é o username, disponível nos atributos do OAuth2User).
@Component
public class OAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User usuario = (OAuth2User) authentication.getPrincipal();
        String login = usuario.getAttribute("login");

        String destino = login != null
                ? "/app.html?user=" + URLEncoder.encode(login, StandardCharsets.UTF_8)
                : "/app.html";

        response.sendRedirect(destino);
    }
}