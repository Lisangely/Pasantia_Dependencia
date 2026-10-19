package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LibroRepository
        extends JpaRepository<Libro, Integer> {
}