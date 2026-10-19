package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.Autor;
import com.fau.biblioteca_api.service.AutorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autores")
public class AutorController {

    private final AutorService autorService;

    public AutorController(
            AutorService autorService) {

        this.autorService = autorService;
    }

    @GetMapping
    public List<Autor> listar() {
        return autorService.obtenerTodos();
    }

    @GetMapping("/{id}")
    public Autor obtenerPorId(
            @PathVariable Integer id) {

        return autorService.obtenerPorId(id)
                .orElseThrow(() ->
                        new RuntimeException("Autor no encontrado"));
    }

    @PostMapping
    public Autor crear(
            @RequestBody Autor autor) {

        return autorService.guardar(autor);
    }

    @PutMapping("/{id}")
    public Autor actualizar(
            @PathVariable Integer id,
            @RequestBody Autor autor) {

        return autorService.actualizar(id, autor);
    }

    @DeleteMapping("/{id}")
    public String eliminar(
            @PathVariable Integer id) {

        autorService.eliminar(id);

        return "Autor eliminado correctamente";
    }
}