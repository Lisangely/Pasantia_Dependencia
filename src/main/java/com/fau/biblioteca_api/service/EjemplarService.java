package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.EjemplarRequest;
import com.fau.biblioteca_api.entity.AccionAuditoria;
import com.fau.biblioteca_api.entity.Ejemplar;
import com.fau.biblioteca_api.entity.EstadoPrestamo;
import com.fau.biblioteca_api.entity.Libro;
import com.fau.biblioteca_api.entity.Ubicacion;
import com.fau.biblioteca_api.repository.EjemplarRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import com.fau.biblioteca_api.repository.PrestamoRepository;
import com.fau.biblioteca_api.repository.UbicacionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EjemplarService {

    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;
    private final UbicacionRepository ubicacionRepository;
    private final PrestamoRepository prestamoRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioActualService usuarioActualService;

    public EjemplarService(
            EjemplarRepository ejemplarRepository,
            LibroRepository libroRepository,
            UbicacionRepository ubicacionRepository,
            PrestamoRepository prestamoRepository,
            AuditoriaService auditoriaService,
            UsuarioActualService usuarioActualService) {

        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.prestamoRepository = prestamoRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<Ejemplar> listar(Integer idLibro) {
        if (idLibro == null) {
            return ejemplarRepository.findAll();
        }
        return ejemplarRepository.findByLibro_IdLibro(idLibro);
    }

    public Ejemplar obtener(Integer id) {
        return ejemplarRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ejemplar no encontrado"));
    }

    public Ejemplar crear(EjemplarRequest request) {
        if (request.getIdLibro() == null) {
            throw new RuntimeException("El libro es obligatorio");
        }

        Libro libro = libroRepository.findById(request.getIdLibro())
                .orElseThrow(() ->
                        new RuntimeException("Libro no encontrado"));

        Ejemplar ejemplar = new Ejemplar();
        ejemplar.setLibro(libro);
        ejemplar.setUbicacion(buscarUbicacion(request.getIdUbicacion()));
        ejemplar.setNumeroEjemplar(request.getNumeroEjemplar());
        ejemplar.setDisponible(
                request.getDisponible() == null || request.getDisponible());
        ejemplar.setRequiereRevision(
                Boolean.TRUE.equals(request.getRequiereRevision()));
        ejemplar.setFechaCreacion(LocalDateTime.now());

        Ejemplar guardado = ejemplarRepository.save(ejemplar);
        auditoriaService.registrar(
                usuarioActualService.obtener(),
                "LIBROS",
                libro.getIdLibro(),
                AccionAuditoria.INSERT,
                null,
                describir(guardado),
                "Ejemplar"
        );
        return guardado;
    }

    public Ejemplar actualizar(Integer id, EjemplarRequest request) {
        Ejemplar ejemplar = obtener(id);
        String antes = describir(ejemplar);

        if (request.getIdLibro() != null) {
            Libro libro = libroRepository.findById(request.getIdLibro())
                    .orElseThrow(() ->
                            new RuntimeException("Libro no encontrado"));
            ejemplar.setLibro(libro);
        }

        if (request.getIdUbicacion() != null) {
            ejemplar.setUbicacion(buscarUbicacion(request.getIdUbicacion()));
        }

        ejemplar.setNumeroEjemplar(request.getNumeroEjemplar());

        if (Boolean.TRUE.equals(request.getDisponible())
                && prestamoRepository.existsByEjemplar_IdEjemplarAndEstado(
                        id, EstadoPrestamo.prestado)) {

            throw new RuntimeException(
                    "El ejemplar tiene un préstamo activo y no puede marcarse disponible");
        }

        if (request.getDisponible() != null) {
            ejemplar.setDisponible(request.getDisponible());
        }
        if (request.getRequiereRevision() != null) {
            ejemplar.setRequiereRevision(request.getRequiereRevision());
        }

        Ejemplar guardado = ejemplarRepository.save(ejemplar);
        String despues = describir(guardado);
        if (!antes.equals(despues) && guardado.getLibro() != null) {
            auditoriaService.registrar(
                    usuarioActualService.obtener(),
                    "LIBROS",
                    guardado.getLibro().getIdLibro(),
                    AccionAuditoria.UPDATE,
                    antes,
                    despues,
                    "Ejemplar"
            );
        }
        return guardado;
    }

    private String describir(Ejemplar ejemplar) {
        String numero = ejemplar.getNumeroEjemplar() == null ? "sin número" : "Nº " + ejemplar.getNumeroEjemplar();
        String lugar = ejemplar.getUbicacion() == null ? "sin ubicación" : ejemplar.getUbicacion().getUbicacionOriginal();
        String estado = Boolean.TRUE.equals(ejemplar.getDisponible()) ? "disponible" : "no disponible";
        return numero + " · " + lugar + " · " + estado;
    }

    private Ubicacion buscarUbicacion(Integer idUbicacion) {
        if (idUbicacion == null) {
            return null;
        }
        return ubicacionRepository.findById(idUbicacion)
                .orElseThrow(() ->
                        new RuntimeException("Ubicación no encontrada"));
    }
}
