package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.dto.AutorLibroResumen;
import com.fau.biblioteca_api.entity.LibroAutor;
import com.fau.biblioteca_api.entity.LibroAutorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LibroAutorRepository
        extends JpaRepository<LibroAutor, LibroAutorId> {

    List<LibroAutor> findByLibro_IdLibro(Integer idLibro);

    @Query("""
            SELECT new com.fau.biblioteca_api.dto.AutorLibroResumen(
                la.libro.idLibro, a.nombre, a.apellido, a.nombreCompleto)
            FROM LibroAutor la JOIN la.autor a
            """)
    List<AutorLibroResumen> resumen();
}