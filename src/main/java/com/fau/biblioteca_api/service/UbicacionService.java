package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.Ubicacion;
import com.fau.biblioteca_api.repository.UbicacionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UbicacionService {

    private final UbicacionRepository ubicacionRepository;

    public UbicacionService(
            UbicacionRepository ubicacionRepository) {

        this.ubicacionRepository = ubicacionRepository;
    }

    public List<Ubicacion> listar() {
        return ubicacionRepository.findAll();
    }

    public Ubicacion obtener(Integer id) {

        return ubicacionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ubicación no encontrada"));
    }

    public Ubicacion crear(
            Ubicacion ubicacion) {

        return ubicacionRepository.save(
                ubicacion);
    }

    public Ubicacion actualizar(
            Integer id,
            Ubicacion datos) {

        Ubicacion ubicacion =
                ubicacionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ubicación no encontrada"));

        ubicacion.setUbicacionOriginal(
                datos.getUbicacionOriginal());

        ubicacion.setEstanteria(
                datos.getEstanteria());

        ubicacion.setPosicion(
                datos.getPosicion());

        ubicacion.setRequiereRevision(
                datos.getRequiereRevision());

        return ubicacionRepository.save(
                ubicacion);
    }

    public void eliminar(Integer id) {

        Ubicacion ubicacion =
                ubicacionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ubicación no encontrada"));

        ubicacionRepository.delete(
                ubicacion);
    }
}