package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.Autor;
import com.fau.biblioteca_api.repository.AutorRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AutorService {

    private final AutorRepository autorRepository;

    public AutorService(
            AutorRepository autorRepository) {

        this.autorRepository = autorRepository;
    }

    public List<Autor> obtenerTodos() {
        return autorRepository.findAll();
    }

    public Optional<Autor> obtenerPorId(Integer id) {
        return autorRepository.findById(id);
    }

    public Autor guardar(Autor autor) {
        return autorRepository.save(autor);
    }

    public Autor actualizar(
        Integer id,
        Autor autorActualizado) {

        Autor autor = autorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Autor no encontrado"));

        autor.setNombreCompleto(
                autorActualizado.getNombreCompleto());

        return autorRepository.save(autor);
    }

    public void eliminar(Integer id) {

        Autor autor = autorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Autor no encontrado"));

        autorRepository.delete(autor);
    }
}