package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.dto.PrestamoRequest;
import com.fau.biblioteca_api.entity.Prestamo;
import com.fau.biblioteca_api.service.PrestamoService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
public class PrestamoController {

    private final PrestamoService prestamoService;

    public PrestamoController(
            PrestamoService prestamoService) {

        this.prestamoService = prestamoService;
    }

    @GetMapping
    public List<Prestamo> listar() {
        return prestamoService.listar();
    }

    @PostMapping
    public Prestamo crear(
            @RequestBody PrestamoRequest request) {

        return prestamoService.crear(request);
    }

    @PutMapping("/{id}/devolver")
    public Prestamo devolver(
            @PathVariable Integer id) {

        return prestamoService.devolver(id);
    }
}