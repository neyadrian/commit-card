package com.commitcard.app.controller;

import com.commitcard.app.dto.CertificadoRequestDTO;
import com.commitcard.app.dto.CurriculoRequestDTO;
import com.commitcard.app.dto.LinkedinRequestDTO;
import com.commitcard.app.model.Certificado;
import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.CertificadoRepository;
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
    private final CertificadoRepository certificadoRepository;

    public PortfolioController(PortfolioSyncService syncService, UserRepository userRepository,
                               CurriculumPdfService curriculumPdfService, ProjectRepository projectRepository,
                               CertificadoRepository certificadoRepository) {
        this.syncService = syncService;
        this.userRepository = userRepository;
        this.curriculumPdfService = curriculumPdfService;
        this.projectRepository = projectRepository;
        this.certificadoRepository = certificadoRepository;
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

    // Adiciona um certificado/prêmio ao portfólio. Só o dono pode adicionar.
    @PostMapping("/{githubUsername}/certificados")
    public ResponseEntity<?> adicionarCertificado(@PathVariable String githubUsername,
                                                  @RequestBody CertificadoRequestDTO dados,
                                                  @AuthenticationPrincipal OAuth2User principal) {
        if (!ehDono(principal, githubUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode adicionar certificados ao seu próprio perfil.");
        }

        Optional<User> userOptional = userRepository.findByGithubUsername(githubUsername);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Certificado certificado = new Certificado(
                dados.getTitulo(), dados.getInstituicao(), dados.getData(), dados.getLinkCredencial(), userOptional.get()
        );
        Certificado salvo = certificadoRepository.save(certificado);

        return ResponseEntity.ok(salvo);
    }

    // Remove um certificado. Só o dono pode remover.
    @DeleteMapping("/certificados/{id}")
    public ResponseEntity<?> removerCertificado(@PathVariable Long id,
                                                @AuthenticationPrincipal OAuth2User principal) {
        Optional<Certificado> certificadoOptional = certificadoRepository.findById(id);

        if (certificadoOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Certificado certificado = certificadoOptional.get();

        if (!ehDono(principal, certificado.getUser().getGithubUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode remover certificados do seu próprio perfil.");
        }

        certificadoRepository.delete(certificado);
        return ResponseEntity.noContent().build();
    }

    // Salva/atualiza o link do LinkedIn no perfil. Só o dono pode editar.
    @PatchMapping("/{githubUsername}/linkedin")
    public ResponseEntity<?> atualizarLinkedin(@PathVariable String githubUsername,
                                               @RequestBody LinkedinRequestDTO dados,
                                               @AuthenticationPrincipal OAuth2User principal) {
        if (!ehDono(principal, githubUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode editar o LinkedIn do seu próprio perfil.");
        }

        Optional<User> userOptional = userRepository.findByGithubUsername(githubUsername);
        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        user.setLinkedinUrl(dados.getLinkedinUrl());
        User salvo = userRepository.save(user);

        return ResponseEntity.ok(salvo);
    }

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