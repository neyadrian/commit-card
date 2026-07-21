package com.commitcard.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String githubUsername;
    private String nome;
    private String bio;
    private String avatarUrl;
    private String linkedinUrl;

    @Enumerated(EnumType.STRING)
    private Plano plano = Plano.FREE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Project> projetos = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Certificado> certificados = new ArrayList<>();

    public User(String githubUsername) {
        this.githubUsername = githubUsername;
    }

    public enum Plano {
        FREE,
        PREMIUM
    }
}
