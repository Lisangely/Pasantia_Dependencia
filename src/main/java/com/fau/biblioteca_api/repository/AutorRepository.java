package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AutorRepository
        extends JpaRepository<Autor, Integer> {
}