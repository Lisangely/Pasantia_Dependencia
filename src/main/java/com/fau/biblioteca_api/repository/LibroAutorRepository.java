package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.LibroAutor;
import com.fau.biblioteca_api.entity.LibroAutorId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LibroAutorRepository
        extends JpaRepository<LibroAutor, LibroAutorId> {
}