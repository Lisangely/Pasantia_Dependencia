package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.Editorial;
import com.fau.biblioteca_api.repository.EditorialRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EditorialService {

    private final EditorialRepository editorialRepository;

    public EditorialService(
            EditorialRepository editorialRepository) {

        this.editorialRepository = editorialRepository;
    }

    public List<Editorial> obtenerTodas() {
        return editorialRepository.findAll();
    }

    public Optional<Editorial> obtenerPorId(Integer id) {
        return editorialRepository.findById(id);
    }

    public Editorial guardar(Editorial editorial) {
        return editorialRepository.save(editorial);
    }

    public Editorial actualizar(
            Integer id,
            Editorial editorialActualizada) {

        Editorial editorial = editorialRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Editorial no encontrada"));

        editorial.setNombre(editorialActualizada.getNombre());

        return editorialRepository.save(editorial);
    }

    public void eliminar(Integer id) {

        Editorial editorial = editorialRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Editorial no encontrada"));

        editorialRepository.delete(editorial);
    }
}