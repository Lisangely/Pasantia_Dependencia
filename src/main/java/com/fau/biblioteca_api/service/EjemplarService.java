package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.EjemplarRequest;
import com.fau.biblioteca_api.entity.Ejemplar;
import com.fau.biblioteca_api.entity.Libro;
import com.fau.biblioteca_api.entity.Ubicacion;
import com.fau.biblioteca_api.repository.EjemplarRepository;
import com.fau.biblioteca_api.repository.LibroRepository;
import com.fau.biblioteca_api.repository.UbicacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EjemplarService {

    private final EjemplarRepository ejemplarRepository;
    private final LibroRepository libroRepository;
    private final UbicacionRepository ubicacionRepository;

    public EjemplarService(
            EjemplarRepository ejemplarRepository,
            LibroRepository libroRepository,
            UbicacionRepository ubicacionRepository) {

        this.ejemplarRepository = ejemplarRepository;
        this.libroRepository = libroRepository;
        this.ubicacionRepository = ubicacionRepository;
    }

    public List<Ejemplar> listar() {
        return ejemplarRepository.findAll();
    }

    public Ejemplar obtener(Integer id) {

        return ejemplarRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Ejemplar no encontrado"));
    }

    public Ejemplar crear(EjemplarRequest request) {

        Libro libro = libroRepository.findById(
                request.getIdLibro())
                .orElseThrow(() ->
                        new RuntimeException("Libro no encontrado"));

        Ejemplar ejemplar = new Ejemplar();

        ejemplar.setLibro(libro);

        if (request.getIdUbicacion() != null) {

            Ubicacion ubicacion =
                    ubicacionRepository.findById(
                            request.getIdUbicacion())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Ubicación no encontrada"));

            ejemplar.setUbicacion(ubicacion);
        }

        ejemplar.setNumeroEjemplar(
                request.getNumeroEjemplar());

        ejemplar.setDisponible(
                request.getDisponible());

        ejemplar.setRequiereRevision(
                request.getRequiereRevision());

        return ejemplarRepository.save(ejemplar);
    }
}