package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.Lector;
import com.fau.biblioteca_api.repository.LectorRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LectorService {

    private final LectorRepository lectorRepository;

    public LectorService(
            LectorRepository lectorRepository) {

        this.lectorRepository = lectorRepository;
    }

    public List<Lector> listar() {
        return lectorRepository.findAll();
    }

    public Lector obtener(Integer id) {
        return lectorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Lector no encontrado"));
    }

    public Lector crear(Lector lector) {
        exigirCompleto(lector);
        lector.setNombre(lector.getNombre().trim());
        lector.setApellido(lector.getApellido().trim());
        lector.setCedula(lector.getCedula().trim());
        lector.setCorreo(lector.getCorreo().trim());
        lector.setTelefono(lector.getTelefono().trim());
        lector.setSemestre(opcional(lector.getSemestre()));
        lector.setCarrera(opcional(lector.getCarrera()));
        if (lector.getFechaRegistro() == null) {
            lector.setFechaRegistro(LocalDateTime.now());
        }
        return lectorRepository.save(lector);
    }

    public Lector actualizar(Integer id, Lector datos) {
        exigirCompleto(datos);
        Lector lector = obtener(id);
        lector.setNombre(datos.getNombre().trim());
        lector.setApellido(datos.getApellido().trim());
        lector.setCedula(datos.getCedula().trim());
        lector.setCorreo(datos.getCorreo().trim());
        lector.setTelefono(datos.getTelefono().trim());
        lector.setSemestre(opcional(datos.getSemestre()));
        lector.setCarrera(opcional(datos.getCarrera()));
        return lectorRepository.save(lector);
    }

    public void eliminar(Integer id) {

        Lector lector = obtener(id);

        lectorRepository.delete(lector);
    }

    private void exigirCompleto(Lector lector) {
        List<String> faltan = new ArrayList<>();
        if (esBlanco(lector.getNombre())) faltan.add("nombre");
        if (esBlanco(lector.getApellido())) faltan.add("apellido");
        if (esBlanco(lector.getCedula())) faltan.add("cédula");
        if (esBlanco(lector.getCorreo())) faltan.add("correo");
        if (esBlanco(lector.getTelefono())) faltan.add("teléfono");
        if (!faltan.isEmpty()) {
            throw new RuntimeException("Completa la información del lector: " + String.join(", ", faltan));
        }
    }

    private String opcional(String valor) {
        if (valor == null || valor.isBlank()) return null;
        return valor.trim();
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }
}