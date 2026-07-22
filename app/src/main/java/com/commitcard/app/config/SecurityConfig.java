package com.commitcard.app.config;

import com.commitcard.app.security.OAuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final OAuthSuccessHandler oAuthSuccessHandler;

    public SecurityConfig(OAuthSuccessHandler oAuthSuccessHandler) {
        this.oAuthSuccessHandler = oAuthSuccessHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Padrão "cookie pra header", recomendado pelo próprio Spring Security pra front-ends
        // em JS puro (sem template engine): o backend manda um token num cookie legível por
        // JS (XSRF-TOKEN); o front lê esse cookie e devolve o mesmo valor no header
        // X-XSRF-TOKEN em toda requisição que muda dado. Um site malicioso não consegue ler
        // cookie de outra origem, então não tem como forjar esse header — é isso que barra o CSRF.
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        requestHandler.setCsrfRequestAttributeName(null); // gera/renova o token em toda requisição, não só quando algo pede

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(requestHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // páginas e arquivos estáticos: sempre públicos
                        .requestMatchers(
                                "/", "/index.html", "/app.html",
                                "/styles.css", "/theme.js", "/auth.js", "/logo.svg",
                                "/favicon.ico", "/favicon.png", "/apple-touch-icon.png",
                                "/oauth2/**", "/login/**"
                        ).permitAll()
                        // ver um portfólio e checar se está logado: público
                        .requestMatchers(HttpMethod.GET, "/api/portfolio/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()
                        // rota bonita do portfólio público (commitcard.com.br/usuario) — sempre pública
                        .requestMatchers(HttpMethod.GET, "/*").permitAll()
                        // tudo que muda dado (sincronizar, destacar, gerar currículo) exige login
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuthSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout"))
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}