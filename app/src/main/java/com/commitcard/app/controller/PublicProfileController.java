package com.commitcard.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// Rota "bonita" pro portfólio público: commitcard.com.br/usuario
// em vez de commitcard.com.br/app.html?user=usuario.
//
// O regex dentro de {username} exclui os nomes reservados (arquivos estáticos,
// rotas da API, do login) pra não conflitar com o resto do site — por exemplo,
// evita que "/styles.css" ou "/api" sejam interpretados como um nome de usuário.
@Controller
public class PublicProfileController {

    private static final String RESERVADOS =
            "api|oauth2|login|logout|app\\.html|index\\.html|perfil\\.html|" +
                    "styles\\.css|theme\\.js|auth\\.js|logo\\.svg|favicon\\.ico|favicon\\.png|apple-touch-icon\\.png";

    @GetMapping("/{username:^(?!" + RESERVADOS + ").*$}")
    public String perfilPublico() {
        return "forward:/perfil.html";
    }
}