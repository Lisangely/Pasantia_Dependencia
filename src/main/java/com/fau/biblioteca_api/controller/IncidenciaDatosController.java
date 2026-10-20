package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.dto.IncidenciaRequest;
import com.fau.biblioteca_api.dto.ResolverIncidenciaRequest;
import com.fau.biblioteca_api.entity.IncidenciaDatos;
import com.fau.biblioteca_api.service.IncidenciaDatosService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaDatosController {

    private final IncidenciaDatosService incidenciaService;

    public IncidenciaDatosController(IncidenciaDatosService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping
    public List<IncidenciaDatos> listar(
            @RequestParam(required = false) Boolean resuelto,
            @RequestParam(required = false) Integer idLibro) {

        return incidenciaService.listar(resuelto, idLibro);
    }

    @GetMapping("/{id}")
    public IncidenciaDatos obtener(@PathVariable Integer id) {
        return incidenciaService.obtener(id);
    }

    @PostMapping
    public IncidenciaDatos crear(@RequestBody IncidenciaRequest request) {
        return incidenciaService.crear(request);
    }

    @PutMapping("/{id}/resolver")
    public IncidenciaDatos resolver(
            @PathVariable Integer id,
            @RequestBody ResolverIncidenciaRequest request) {
        return incidenciaService.resolver(id, request.getValor());
    }
}
