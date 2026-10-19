package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.LibroRequest;
import com.fau.biblioteca_api.entity.Editorial;
import com.fau.biblioteca_api.entity.Libro;
import com.fau.biblioteca_api.repository.EditorialRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final EditorialRepository editorialRepository;

    public LibroService(
            LibroRepository libroRepository,
            EditorialRepository editorialRepository) {

        this.libroRepository = libroRepository;
        this.editorialRepository = editorialRepository;
    }

    public List<Libro> listar() {
        return libroRepository.findAll();
    }

    public Libro obtener(Integer id) {

        return libroRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Libro no encontrado"));
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
        libro.setCota(request.getCota());
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

        return libroRepository.save(libro);
    }
}