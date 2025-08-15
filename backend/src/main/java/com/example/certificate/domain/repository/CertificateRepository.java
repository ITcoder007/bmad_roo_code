package com.example.certificate.domain.repository;

import com.example.certificate.domain.model.Certificate;
import com.example.certificate.domain.model.CertificateStatus;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository {
    
    Optional<Certificate> findById(Long id);
    
    List<Certificate> findAll();
    
    Certificate save(Certificate certificate);
    
    void deleteById(Long id);
    
    List<Certificate> findByStatus(CertificateStatus status);
    
    List<Certificate> findByExpiryDateBefore(Date date);
}