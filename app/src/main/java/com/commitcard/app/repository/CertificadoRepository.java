package com.commitcard.app.repository;

import com.commitcard.app.model.Certificado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificadoRepository extends JpaRepository<Certificado, Long> {
}