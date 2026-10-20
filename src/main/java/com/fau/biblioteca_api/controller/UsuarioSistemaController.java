package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.service.UsuarioSistemaService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioSistemaController {

    private final UsuarioSistemaService service;

    public UsuarioSistemaController(UsuarioSistemaService service) {
        this.service = service;
    }

    @PreAuthorize("hasRole('administrador')")
    @GetMapping
    public List<UsuarioSistema> listar() {
        return service.listar();
    }

    @PreAuthorize("hasRole('administrador')")
    @GetMapping("/{id}")
    public UsuarioSistema obtener(@PathVariable Integer id) {
        return service.obtener(id);
    }

    @PreAuthorize("hasRole('administrador')")
    @PostMapping
    public UsuarioSistema crear(@RequestBody UsuarioSistema usuario) {
        return service.crear(usuario);
    }

    @PreAuthorize("hasRole('administrador')")
    @PutMapping("/{id}")
    public UsuarioSistema actualizar(
            @PathVariable Integer id,
            @RequestBody UsuarioSistema usuario) {

        return service.actualizar(id, usuario);
    }
}
