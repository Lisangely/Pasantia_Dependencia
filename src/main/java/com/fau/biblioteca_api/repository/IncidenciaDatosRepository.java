package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.IncidenciaDatos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IncidenciaDatosRepository
        extends JpaRepository<IncidenciaDatos, Integer> {

    @Query("""
            SELECT i FROM IncidenciaDatos i
            WHERE (:resuelto IS NULL OR i.resuelto = :resuelto)
              AND (:idLibro IS NULL OR i.libro.idLibro = :idLibro)
            """)
    List<IncidenciaDatos> buscar(
            @Param("resuelto") Boolean resuelto,
            @Param("idLibro") Integer idLibro);
}
