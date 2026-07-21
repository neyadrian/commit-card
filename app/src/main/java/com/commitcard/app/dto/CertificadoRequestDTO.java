package com.commitcard.app.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CertificadoRequestDTO {
    private String titulo;
    private String instituicao;
    private String data;
    private String linkCredencial;
}