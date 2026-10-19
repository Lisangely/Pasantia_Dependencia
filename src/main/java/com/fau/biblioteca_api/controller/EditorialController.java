package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.Editorial;
import com.fau.biblioteca_api.service.EditorialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/editoriales")
public class EditorialController {

    private final EditorialService editorialService;

    public EditorialController(
            EditorialService editorialService) {

        this.editorialService = editorialService;
    }

    @GetMapping
    public List<Editorial> listar() {
        return editorialService.obtenerTodas();
    }

    @GetMapping("/{id}")
    public Editorial obtenerPorId(
            @PathVariable Integer id) {

        return editorialService.obtenerPorId(id)
                .orElseThrow(() ->
                        new RuntimeException("Editorial no encontrada"));
    }

    @PostMapping
    public Editorial crear(
            @RequestBody Editorial editorial) {

        return editorialService.guardar(editorial);
    }

    @PutMapping("/{id}")
    public Editorial actualizar(
            @PathVariable Integer id,
            @RequestBody Editorial editorial) {

        return editorialService.actualizar(id, editorial);
    }

    @DeleteMapping("/{id}")
    public String eliminar(
            @PathVariable Integer id) {

        editorialService.eliminar(id);

        return "Editorial eliminada correctamente";
    }
}