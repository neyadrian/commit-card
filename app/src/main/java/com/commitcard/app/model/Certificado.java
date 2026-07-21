package com.commitcard.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "certificados")
@Getter
@Setter
@NoArgsConstructor
public class Certificado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String instituicao;
    private String data;
    private String linkCredencial;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Certificado(String titulo, String instituicao, String data, String linkCredencial, User user) {
        this.titulo = titulo;
        this.instituicao = instituicao;
        this.data = data;
        this.linkCredencial = linkCredencial;
        this.user = user;
    }
}