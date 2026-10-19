package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.LibroRequest;
import com.fau.biblioteca_api.entity.*;
import com.fau.biblioteca_api.repository.EditorialRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import com.fau.biblioteca_api.repository.UsuarioSistemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final EditorialRepository editorialRepository;
    private final UsuarioSistemaRepository usuarioRepository;
    private final AuditoriaService auditoriaService;

    public LibroService(
            LibroRepository libroRepository,
            EditorialRepository editorialRepository,
            UsuarioSistemaRepository usuarioRepository,
            AuditoriaService auditoriaService) {

        this.libroRepository = libroRepository;
        this.editorialRepository = editorialRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro obtener(Integer id) {

        return libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Libro no encontrado"));
    }

    public Libro crear(LibroRequest request) {

        Libro libro = new Libro();

        libro.setTitulo(request.getTitulo());
        libro.setTipo(request.getTipo());
        libro.setFechaPublicacion(
                request.getFechaPublicacion());

        libro.setFechaOriginal(
                request.getFechaOriginal());

        libro.setRequiereRevisionFecha(
                request.getRequiereRevisionFecha());

        libro.setCota(
                request.getCota());

        libro.setObservaciones(
                request.getObservaciones());

        if (request.getIdEditorial() != null) {

            Editorial editorial =
                    editorialRepository.findById(
                            request.getIdEditorial())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Editorial no encontrada"));

            libro.setEditorial(editorial);
        }

        Libro libroGuardado =
                libroRepository.save(libro);

        auditoriaService.registrar(
                obtenerUsuarioAuditoria(),
                "LIBROS",
                libroGuardado.getIdLibro(),
                AccionAuditoria.INSERT,
                null,
                libroGuardado.getTitulo(),
                "Libro creado"
        );

        return libroGuardado;
    }

    public Libro actualizar(
            Integer id,
            LibroRequest request) {

        Libro libro =
                libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Libro no encontrado"));

        String valorAnterior =
                libro.getTitulo();

        libro.setTitulo(request.getTitulo());
        libro.setTipo(request.getTipo());

        libro.setFechaPublicacion(
                request.getFechaPublicacion());

        libro.setFechaOriginal(
                request.getFechaOriginal());

        libro.setRequiereRevisionFecha(
                request.getRequiereRevisionFecha());

        libro.setCota(
                request.getCota());

        libro.setObservaciones(
                request.getObservaciones());

        if (request.getIdEditorial() != null) {

            Editorial editorial =
                    editorialRepository.findById(
                            request.getIdEditorial())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Editorial no encontrada"));

            libro.setEditorial(editorial);
        } else {

            libro.setEditorial(null);
        }

        Libro libroActualizado =
                libroRepository.save(libro);

        auditoriaService.registrar(
                obtenerUsuarioAuditoria(),
                "LIBROS",
                libroActualizado.getIdLibro(),
                AccionAuditoria.UPDATE,
                valorAnterior,
                libroActualizado.getTitulo(),
                "Libro actualizado"
        );

        return libroActualizado;
    }

    public void eliminar(Integer id) {

        Libro libro =
                libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Libro no encontrado"));

        auditoriaService.registrar(
                obtenerUsuarioAuditoria(),
                "LIBROS",
                libro.getIdLibro(),
                AccionAuditoria.DELETE,
                libro.getTitulo(),
                null,
                "Libro eliminado"
        );

        libroRepository.delete(libro);
    }

    private UsuarioSistema obtenerUsuarioAuditoria() {

        return usuarioRepository.findById(1)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario de auditoría no encontrado"));
    }
}