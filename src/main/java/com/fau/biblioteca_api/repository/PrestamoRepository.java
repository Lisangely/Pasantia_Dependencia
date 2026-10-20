package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.EstadoPrestamo;
import com.fau.biblioteca_api.entity.Prestamo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrestamoRepository
        extends JpaRepository<Prestamo, Integer> {

    boolean existsByEjemplar_IdEjemplarAndEstado(
            Integer idEjemplar,
            EstadoPrestamo estado);
}