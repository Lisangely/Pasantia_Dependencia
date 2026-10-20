package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditoriaRepository
        extends JpaRepository<Auditoria, Integer> {

    List<Auditoria> findByEntidadAndIdEntidadOrderByFechaEventoDesc(
            String entidad,
            Integer idEntidad);
}