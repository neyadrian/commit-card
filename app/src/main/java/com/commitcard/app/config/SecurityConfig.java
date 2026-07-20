package com.commitcard.app.config;

import com.commitcard.app.security.OAuthSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
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
        http
                // CSRF desligado de propósito: o front é HTML+JS puro, sem token CSRF
                // configurado nas chamadas fetch. Aceitável pra um projeto pessoal;
                // numa aplicação real de produção o ideal é reabilitar e propagar
                // o token no front (ou usar autenticação via token/JWT em vez de sessão).
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // páginas e arquivos estáticos: sempre públicos
                        .requestMatchers(
                                "/", "/index.html", "/app.html",
                                "/styles.css", "/theme.js", "/auth.js", "/logo.svg",
                                "/favicon.ico",
                                "/oauth2/**", "/login/**"
                        ).permitAll()
                        // ver um portfólio e checar se está logado: público
                        .requestMatchers(HttpMethod.GET, "/api/portfolio/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()
                        // tudo que muda dado (sincronizar, destacar, gerar currículo) exige login
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler(oAuthSuccessHandler)
                )
                .logout(logout -> logout
                        // Spring Security 7 substituiu AntPathRequestMatcher por PathPatternRequestMatcher
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.GET, "/logout"))
                        .logoutSuccessUrl("/")
                );

        return http.build();
    }
}