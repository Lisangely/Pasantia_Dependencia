package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository
        extends JpaRepository<Auditoria, Integer> {
}