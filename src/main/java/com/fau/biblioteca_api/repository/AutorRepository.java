package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AutorRepository
        extends JpaRepository<Autor, Integer> {

    Optional<Autor> findFirstByNombreCompletoIgnoreCase(String nombreCompleto);

    @Query("""
            SELECT a FROM Autor a
            WHERE lower(coalesce(a.nombreCompleto, '')) = lower(:texto)
               OR lower(trim(concat(a.nombre, ' ', coalesce(a.apellido, '')))) = lower(:texto)
            """)
    List<Autor> buscarPorTexto(@Param("texto") String texto);
}