package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.Lector;
import com.fau.biblioteca_api.repository.LectorRepository;
import org.springframework.stereotype.Service;

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
        return lectorRepository.save(lector);
    }

    public Lector actualizar(Integer id, Lector datos) {

        Lector lector = obtener(id);

        lector.setNombre(datos.getNombre());
        lector.setApellido(datos.getApellido());
        lector.setCedula(datos.getCedula());
        lector.setCorreo(datos.getCorreo());
        lector.setTelefono(datos.getTelefono());

        return lectorRepository.save(lector);
    }

    public void eliminar(Integer id) {

        Lector lector = obtener(id);

        lectorRepository.delete(lector);
    }
}