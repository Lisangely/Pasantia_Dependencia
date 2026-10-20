package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.AutorLibroResumen;
import com.fau.biblioteca_api.dto.LibroAutorRequest;
import com.fau.biblioteca_api.entity.AccionAuditoria;
import com.fau.biblioteca_api.entity.Autor;
import com.fau.biblioteca_api.entity.Libro;
import com.fau.biblioteca_api.entity.LibroAutor;
import com.fau.biblioteca_api.entity.LibroAutorId;
import com.fau.biblioteca_api.repository.AutorRepository;
import com.fau.biblioteca_api.repository.LibroAutorRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LibroAutorService {

    private final LibroAutorRepository libroAutorRepository;
    private final LibroRepository libroRepository;
    private final AutorRepository autorRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioActualService usuarioActualService;

    public LibroAutorService(
            LibroAutorRepository libroAutorRepository,
            LibroRepository libroRepository,
            AutorRepository autorRepository,
            AuditoriaService auditoriaService,
            UsuarioActualService usuarioActualService) {

        this.libroAutorRepository = libroAutorRepository;
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<AutorLibroResumen> resumen() {
        return libroAutorRepository.resumen();
    }

    public List<LibroAutor> listar(Integer idLibro) {
        if (idLibro == null) {
            return libroAutorRepository.findAll();
        }
        return libroAutorRepository.findByLibro_IdLibro(idLibro);
    }

    public LibroAutor asociar(LibroAutorRequest request) {
        if (request.getIdLibro() == null || request.getIdAutor() == null) {
            throw new RuntimeException("El libro y el autor son obligatorios");
        }

        Libro libro = libroRepository.findById(request.getIdLibro())
                .orElseThrow(() -> new RuntimeException("Libro no encontrado"));

        Autor autor = autorRepository.findById(request.getIdAutor())
                .orElseThrow(() -> new RuntimeException("Autor no encontrado"));

        LibroAutorId id = new LibroAutorId(libro.getIdLibro(), autor.getIdAutor());

        return libroAutorRepository.findById(id)
                .orElseGet(() -> {
                    LibroAutor libroAutor = new LibroAutor();
                    libroAutor.setLibro(libro);
                    libroAutor.setAutor(autor);
                    LibroAutor guardado = libroAutorRepository.save(libroAutor);
                    auditoriaService.registrar(
                            usuarioActualService.obtener(),
                            "LIBROS",
                            libro.getIdLibro(),
                            AccionAuditoria.INSERT,
                            null,
                            nombreAutor(autor),
                            "Autor"
                    );
                    return guardado;
                });
    }

    @Transactional
    public void eliminar(Integer idLibro, Integer idAutor) {
        LibroAutorId id = new LibroAutorId(idLibro, idAutor);
        LibroAutor vinculo = libroAutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La relación libro-autor no existe"));
        auditoriaService.registrar(
                usuarioActualService.obtener(),
                "LIBROS",
                idLibro,
                AccionAuditoria.DELETE,
                nombreAutor(vinculo.getAutor()),
                null,
                "Autor"
        );
        libroAutorRepository.delete(vinculo);
    }

    private String nombreAutor(Autor autor) {
        if (autor.getNombreCompleto() != null && !autor.getNombreCompleto().isBlank()) {
            return autor.getNombreCompleto();
        }
        String apellido = autor.getApellido() == null ? "" : " " + autor.getApellido();
        return (autor.getNombre() + apellido).trim();
    }
}
