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

    public EjemplarController(
            EjemplarService ejemplarService) {

        this.ejemplarService = ejemplarService;
    }

    @GetMapping
    public List<Ejemplar> listar() {
        return ejemplarService.listar();
    }

    @GetMapping("/{id}")
    public Ejemplar obtener(
            @PathVariable Integer id) {

        return ejemplarService.obtener(id);
    }

    @PostMapping
    public Ejemplar crear(
            @RequestBody EjemplarRequest request) {

        return ejemplarService.crear(request);
    }
}