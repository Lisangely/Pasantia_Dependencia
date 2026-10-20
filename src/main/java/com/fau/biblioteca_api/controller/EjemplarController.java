package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.dto.EjemplarRequest;
import com.fau.biblioteca_api.entity.Ejemplar;
import com.fau.biblioteca_api.service.EjemplarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ejemplares")
public class EjemplarController {

    private final EjemplarService ejemplarService;

    public EjemplarController(EjemplarService ejemplarService) {
        this.ejemplarService = ejemplarService;
    }

    @GetMapping
    public List<Ejemplar> listar(
            @RequestParam(required = false) Integer idLibro) {

        return ejemplarService.listar(idLibro);
    }

    @GetMapping("/{id}")
    public Ejemplar obtener(@PathVariable Integer id) {
        return ejemplarService.obtener(id);
    }

    @PostMapping
    public Ejemplar crear(@RequestBody EjemplarRequest request) {
        return ejemplarService.crear(request);
    }

    @PutMapping("/{id}")
    public Ejemplar actualizar(
            @PathVariable Integer id,
            @RequestBody EjemplarRequest request) {

        return ejemplarService.actualizar(id, request);
    }
}
