package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Ejemplar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EjemplarRepository
        extends JpaRepository<Ejemplar, Integer> {

    List<Ejemplar> findByLibro_IdLibro(Integer idLibro);
}