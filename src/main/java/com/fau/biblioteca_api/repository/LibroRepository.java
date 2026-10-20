package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LibroRepository
        extends JpaRepository<Libro, Integer> {

    @Query("""
            SELECT DISTINCT l FROM Libro l
            LEFT JOIN LibroAutor la ON la.libro = l
            LEFT JOIN la.autor a
            WHERE (cast(:titulo as string) IS NULL OR LOWER(l.titulo) LIKE LOWER(CONCAT(cast(:titulo as string), '%')))
              AND (cast(:tipo as string) IS NULL OR LOWER(l.tipo) LIKE LOWER(CONCAT(cast(:tipo as string), '%')))
              AND (cast(:cota as string) IS NULL OR LOWER(l.cota) LIKE LOWER(CONCAT(cast(:cota as string), '%')))
              AND (
                    cast(:autor as string) IS NULL
                    OR LOWER(COALESCE(a.nombreCompleto, '')) LIKE LOWER(CONCAT(cast(:autor as string), '%'))
                    OR LOWER(COALESCE(a.nombre, '')) LIKE LOWER(CONCAT(cast(:autor as string), '%'))
                    OR LOWER(COALESCE(a.apellido, '')) LIKE LOWER(CONCAT(cast(:autor as string), '%'))
              )
            """)
    List<Libro> buscar(
            @Param("titulo") String titulo,
            @Param("autor") String autor,
            @Param("cota") String cota,
            @Param("tipo") String tipo);
}