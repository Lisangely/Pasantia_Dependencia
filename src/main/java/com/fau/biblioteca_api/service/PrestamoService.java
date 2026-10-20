package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.dto.PrestamoRequest;
import com.fau.biblioteca_api.entity.*;
import com.fau.biblioteca_api.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final EjemplarRepository ejemplarRepository;
    private final LectorRepository lectorRepository;
    private final AuditoriaService auditoriaService;
    private final UsuarioActualService usuarioActualService;

    public PrestamoService(
            PrestamoRepository prestamoRepository,
            EjemplarRepository ejemplarRepository,
            LectorRepository lectorRepository,
            AuditoriaService auditoriaService,
            UsuarioActualService usuarioActualService) {

        this.prestamoRepository = prestamoRepository;
        this.ejemplarRepository = ejemplarRepository;
        this.lectorRepository = lectorRepository;
        this.auditoriaService = auditoriaService;
        this.usuarioActualService = usuarioActualService;
    }

    public List<Prestamo> listar() {
        return prestamoRepository.findAll();
    }

    @Transactional
    public Prestamo crear(PrestamoRequest request) {
        Ejemplar ejemplar = ejemplarRepository.findById(request.getIdEjemplar())
                .orElseThrow(() ->
                        new RuntimeException("Ejemplar no encontrado"));

        if (!Boolean.TRUE.equals(ejemplar.getDisponible())) {
            throw new RuntimeException("El ejemplar no está disponible");
        }

        if (prestamoRepository.existsByEjemplar_IdEjemplarAndEstado(
                ejemplar.getIdEjemplar(),
                EstadoPrestamo.prestado)) {

            throw new RuntimeException("El ejemplar ya tiene un préstamo activo");
        }

        Lector lector = lectorRepository.findById(request.getIdLector())
                .orElseThrow(() ->
                        new RuntimeException("Lector no encontrado"));

        UsuarioSistema usuario = usuarioActualService.obtener();

        Prestamo prestamo = new Prestamo();
        prestamo.setEjemplar(ejemplar);
        prestamo.setLector(lector);
        prestamo.setRegistradoPor(usuario);
        prestamo.setFechaPrestamo(LocalDateTime.now());
        prestamo.setFechaDevolucionEsperada(request.getFechaDevolucionEsperada());
        prestamo.setEstado(EstadoPrestamo.prestado);

        ejemplar.setDisponible(false);
        ejemplarRepository.save(ejemplar);

        Prestamo guardado = prestamoRepository.save(prestamo);

        auditoriaService.registrar(
                usuario,
                "PRESTAMOS",
                guardado.getIdPrestamo(),
                AccionAuditoria.PRESTAMO,
                null,
                ejemplar.getIdEjemplar().toString(),
                "Préstamo registrado"
        );

        return guardado;
    }

    @Transactional
    public Prestamo devolver(Integer idPrestamo) {
        Prestamo prestamo = prestamoRepository.findById(idPrestamo)
                .orElseThrow(() ->
                        new RuntimeException("Préstamo no encontrado"));

        if (prestamo.getEstado() == EstadoPrestamo.devuelto) {
            throw new RuntimeException("El préstamo ya fue devuelto");
        }

        prestamo.setEstado(EstadoPrestamo.devuelto);
        prestamo.setFechaDevolucionReal(LocalDateTime.now());

        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setDisponible(true);
        ejemplarRepository.save(ejemplar);

        Prestamo guardado = prestamoRepository.save(prestamo);
        UsuarioSistema usuario = usuarioActualService.obtener();

        auditoriaService.registrar(
                usuario,
                "PRESTAMOS",
                guardado.getIdPrestamo(),
                AccionAuditoria.DEVOLUCION,
                EstadoPrestamo.prestado.name(),
                EstadoPrestamo.devuelto.name(),
                "Devolución registrada"
        );

        return guardado;
    }
}
