package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.repository.UsuarioSistemaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioSistemaService {

    private final UsuarioSistemaRepository repository;

    public UsuarioSistemaService(
            UsuarioSistemaRepository repository) {

        this.repository = repository;
    }

    public List<UsuarioSistema> listar() {
        return repository.findAll();
    }

    public UsuarioSistema obtener(Integer id) {

        return repository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));
    }

    public UsuarioSistema crear(
            UsuarioSistema usuario) {

        return repository.save(usuario);
    }
}