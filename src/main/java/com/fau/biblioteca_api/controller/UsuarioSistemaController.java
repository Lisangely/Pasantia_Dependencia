package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.service.UsuarioSistemaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioSistemaController {

    private final UsuarioSistemaService service;

    public UsuarioSistemaController(
            UsuarioSistemaService service) {

        this.service = service;
    }

    @GetMapping
    public List<UsuarioSistema> listar() {
        return service.listar();
    }

    @GetMapping("/{id}")
    public UsuarioSistema obtener(
            @PathVariable Integer id) {

        return service.obtener(id);
    }

    @PostMapping
    public UsuarioSistema crear(
            @RequestBody UsuarioSistema usuario) {

        return service.crear(usuario);
    }
}