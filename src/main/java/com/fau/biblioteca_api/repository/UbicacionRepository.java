package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UbicacionRepository
        extends JpaRepository<Ubicacion, Integer> {
}