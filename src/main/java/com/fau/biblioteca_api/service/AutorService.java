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
        normalizar(autor);
        return autorRepository.save(autor);
    }

    public Autor actualizar(
        Integer id,
        Autor autorActualizado) {

        Autor autor = autorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Autor no encontrado"));

        autor.setNombre(autorActualizado.getNombre());
        autor.setApellido(autorActualizado.getApellido());
        autor.setNombreCompleto(autorActualizado.getNombreCompleto());
        normalizar(autor);

        return autorRepository.save(autor);
    }

    private void normalizar(Autor autor) {
        if (esBlanco(autor.getNombre()) && !esBlanco(autor.getNombreCompleto())) {
            autor.setNombre(autor.getNombreCompleto().trim());
        }
        if (esBlanco(autor.getNombre())) {
            throw new RuntimeException("El nombre del autor es obligatorio");
        }
        autor.setNombre(autor.getNombre().trim());
        if (autor.getApellido() != null) {
            autor.setApellido(autor.getApellido().trim());
        }
        if (esBlanco(autor.getNombreCompleto())) {
            String apellido = esBlanco(autor.getApellido()) ? "" : " " + autor.getApellido();
            autor.setNombreCompleto((autor.getNombre() + apellido).trim());
        } else {
            autor.setNombreCompleto(autor.getNombreCompleto().trim());
        }
    }

    private boolean esBlanco(String valor) {
        return valor == null || valor.isBlank();
    }

    public void eliminar(Integer id) {

        Autor autor = autorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Autor no encontrado"));

        autorRepository.delete(autor);
    }
}