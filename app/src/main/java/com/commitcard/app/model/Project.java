package com.commitcard.app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nomeRepositorio;

    @Column(length = 512)
    private String descricao;

    private String linguagemPrincipal;
    private Integer stars;
    private Integer forks;
    private String urlRepositorio;
    private boolean destaque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Project(String nomeRepositorio, String urlRepositorio, User user) {
        this.nomeRepositorio = nomeRepositorio;
        this.urlRepositorio = urlRepositorio;
        this.user = user;
        this.stars = 0;
        this.forks = 0;
        this.destaque = false;
    }
}
