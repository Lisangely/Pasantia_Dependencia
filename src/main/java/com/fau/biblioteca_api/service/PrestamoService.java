package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.PrestamoRequest;
import com.fau.biblioteca_api.entity.*;
import com.fau.biblioteca_api.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final EjemplarRepository ejemplarRepository;
    private final LectorRepository lectorRepository;
    private final UsuarioSistemaRepository usuarioRepository;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            EjemplarRepository ejemplarRepository,
            LectorRepository lectorRepository,
            UsuarioSistemaRepository usuarioRepository) {

        this.prestamoRepository = prestamoRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.lectorRepository = lectorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    public Prestamo crear(
            PrestamoRequest request) {

        Ejemplar ejemplar =
                ejemplarRepository.findById(
                        request.getIdEjemplar())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ejemplar no encontrado"));

        if (!Boolean.TRUE.equals(
                ejemplar.getDisponible())) {

            throw new RuntimeException(
                    "El ejemplar no está disponible");
        }

        Lector lector =
                lectorRepository.findById(
                        request.getIdLector())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Lector no encontrado"));

        UsuarioSistema usuario =
                usuarioRepository.findById(
                        request.getRegistradoPor())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Usuario no encontrado"));

        Prestamo prestamo = new Prestamo();

        prestamo.setEjemplar(ejemplar);
        prestamo.setLector(lector);
        prestamo.setRegistradoPor(usuario);

        prestamo.setFechaPrestamo(
                LocalDateTime.now());

        prestamo.setFechaDevolucionEsperada(
                request.getFechaDevolucionEsperada());

        prestamo.setEstado(
                EstadoPrestamo.prestado);

        ejemplar.setDisponible(false);

        ejemplarRepository.save(ejemplar);

        return prestamoRepository.save(prestamo);
    }

    public Prestamo devolver(
            Integer idPrestamo) {

        Prestamo prestamo =
                prestamoRepository.findById(idPrestamo)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Préstamo no encontrado"));

        prestamo.setEstado(
                EstadoPrestamo.devuelto);

        prestamo.setFechaDevolucionReal(
                LocalDateTime.now());

        Ejemplar ejemplar =
                prestamo.getEjemplar();

        ejemplar.setDisponible(true);

        ejemplarRepository.save(ejemplar);

        return prestamoRepository.save(prestamo);
    }
}