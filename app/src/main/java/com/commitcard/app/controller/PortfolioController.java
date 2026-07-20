package com.commitcard.app.controller;

import com.commitcard.app.model.Project;
import com.commitcard.app.model.User;
import com.commitcard.app.repository.ProjectRepository;
import com.commitcard.app.repository.UserRepository;
import com.commitcard.app.service.CurriculumPdfService;
import com.commitcard.app.service.PortfolioSyncService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/sync/{githubUsername}")
    public User sincronizar(@PathVariable String githubUsername) {
        return syncService.sincronizar(githubUsername);
    }

    @GetMapping("/{githubUsername}")
    public ResponseEntity<User> buscarPortfolio(@PathVariable String githubUsername) {
        return userRepository.findByGithubUsername(githubUsername)
                .map(user -> ResponseEntity.ok(user))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{githubUsername}/curriculo")
    public ResponseEntity<byte[]> baixarCurriculo(@PathVariable String githubUsername) {
        Optional<User> userOptional = userRepository.findByGithubUsername(githubUsername);

        if (userOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOptional.get();
        byte[] pdfBytes = curriculumPdfService.gerarPdf(user);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=curriculo.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PatchMapping("/projects/{id}/destaque")
    public ResponseEntity<Project> alternarDestaque(@PathVariable Long id) {
        Optional<Project> projectOptional = projectRepository.findById(id);

        if (projectOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Project project = projectOptional.get();
        project.setDestaque(!project.isDestaque());
        Project projectSalvo = projectRepository.save(project);

        return ResponseEntity.ok(projectSalvo);
    }
}