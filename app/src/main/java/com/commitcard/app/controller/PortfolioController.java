package com.commitcard.app.controller;

import com.commitcard.app.model.User;
import com.commitcard.app.repository.UserRepository;
import com.commitcard.app.service.PortfolioSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioSyncService syncService;
    private final UserRepository userRepository;

    public PortfolioController(PortfolioSyncService syncService, UserRepository userRepository) {
        this.syncService = syncService;
        this.userRepository = userRepository;
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
}