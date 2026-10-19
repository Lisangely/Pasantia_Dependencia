package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.Lector;
import com.fau.biblioteca_api.service.LectorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lectores")
public class LectorController {

    private final LectorService lectorService;

    public LectorController(
            LectorService lectorService) {

        this.lectorService = lectorService;
    }

    @GetMapping
    public List<Lector> listar() {
        return lectorService.listar();
    }

    @GetMapping("/{id}")
    public Lector obtener(
            @PathVariable Integer id) {

        return lectorService.obtener(id);
    }

    @PostMapping
    public Lector crear(
            @RequestBody Lector lector) {

        return lectorService.crear(lector);
    }

    @PutMapping("/{id}")
    public Lector actualizar(
            @PathVariable Integer id,
            @RequestBody Lector lector) {

        return lectorService.actualizar(id, lector);
    }

    @DeleteMapping("/{id}")
    public String eliminar(
            @PathVariable Integer id) {

        lectorService.eliminar(id);

        return "Lector eliminado correctamente";
    }
}