package com.commitcard.app.controller;

import com.commitcard.app.dto.CurriculoRequestDTO;
import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.ProjectRepository;
import com.commitcard.app.repository.UserRepository;
import com.commitcard.app.service.CurriculumPdfService;
import com.commitcard.app.service.PortfolioSyncService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioSyncService syncService;
    private final UserRepository userRepository;
    private final CurriculumPdfService curriculumPdfService;
    private final ProjectRepository projectRepository;

    public PortfolioController(PortfolioSyncService syncService, UserRepository userRepository,
                               CurriculumPdfService curriculumPdfService, ProjectRepository projectRepository) {
        this.syncService = syncService;
        this.userRepository = userRepository;
        this.curriculumPdfService = curriculumPdfService;
        this.projectRepository = projectRepository;
    }

    // Só o dono (logado com o mesmo usuário do GitHub) pode sincronizar.
    @PostMapping("/sync/{githubUsername}")
    public ResponseEntity<?> sincronizar(@PathVariable String githubUsername,
                                         @AuthenticationPrincipal OAuth2User principal) {
        if (!ehDono(principal, githubUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode sincronizar o seu próprio perfil do GitHub.");
        }

        User user = syncService.sincronizar(githubUsername);
        return ResponseEntity.ok(user);
    }

    // Ver um portfólio é sempre público, não exige login.
    @GetMapping("/{githubUsername}")
    public ResponseEntity<User> buscarPortfolio(@PathVariable String githubUsername) {
        return userRepository.findByGithubUsername(githubUsername)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    // Exportação rápida: só o dono pode gerar o próprio currículo.
    @GetMapping("/{githubUsername}/curriculo")
    public ResponseEntity<?> baixarCurriculoRapido(@PathVariable String githubUsername,
                                                   @AuthenticationPrincipal OAuth2User principal) {
        if (!ehDono(principal, githubUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode gerar o currículo do seu próprio perfil.");
        }

        Optional<User> userOptional = userRepository.findByGithubUsername(githubUsername);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = curriculumPdfService.gerarPdf(userOptional.get());
        return respostaPdf(pdfBytes);
    }

    // Exportação completa com dados do formulário: também só o dono.
    @PostMapping("/{githubUsername}/curriculo")
    public ResponseEntity<?> baixarCurriculoCompleto(@PathVariable String githubUsername,
                                                     @RequestBody CurriculoRequestDTO dados,
                                                     @AuthenticationPrincipal OAuth2User principal) {
        if (!ehDono(principal, githubUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode gerar o currículo do seu próprio perfil.");
        }

        Optional<User> userOptional = userRepository.findByGithubUsername(githubUsername);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        byte[] pdfBytes = curriculumPdfService.gerarPdfCompleto(userOptional.get(), dados);
        return respostaPdf(pdfBytes);
    }

    // Alternar destaque: precisa ser dono do projeto (comparando com o user dele).
    @PatchMapping("/projects/{id}/destaque")
    public ResponseEntity<?> alternarDestaque(@PathVariable Long id,
                                              @AuthenticationPrincipal OAuth2User principal) {
        Optional<Project> projectOptional = projectRepository.findById(id);

        if (projectOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOptional.get();

        if (!ehDono(principal, project.getUser().getGithubUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode gerenciar destaques do seu próprio perfil.");
        }

        project.setDestaque(!project.isDestaque());
        Project projectSalvo = projectRepository.save(project);

        return ResponseEntity.ok(projectSalvo);
    }

    // Compara o usuário logado (via OAuth2, atributo "login" do GitHub) com o username da rota.
    private boolean ehDono(OAuth2User principal, String githubUsername) {
        if (principal == null || githubUsername == null) return false;
        String loginAutenticado = principal.getAttribute("login");
        return loginAutenticado != null && loginAutenticado.equalsIgnoreCase(githubUsername);
    }

    private ResponseEntity<byte[]> respostaPdf(byte[] pdfBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=curriculo.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}