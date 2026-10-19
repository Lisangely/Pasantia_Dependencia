package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.Auditoria;
import com.fau.biblioteca_api.service.AuditoriaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(
            AuditoriaService auditoriaService) {

        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    public List<Auditoria> listar() {

        return auditoriaService.listar();
    }

    @GetMapping("/{id}")
    public Auditoria obtener(
            @PathVariable Integer id) {

        return auditoriaService.obtener(id);
    }
}