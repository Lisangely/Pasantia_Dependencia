package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.entity.Ubicacion;
import com.fau.biblioteca_api.service.UbicacionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ubicaciones")
public class UbicacionController {

    private final UbicacionService ubicacionService;

    public UbicacionController(
            UbicacionService ubicacionService) {

        this.ubicacionService = ubicacionService;
    }

    @GetMapping
    public List<Ubicacion> listar() {
        return ubicacionService.listar();
    }

    @GetMapping("/{id}")
    public Ubicacion obtener(
            @PathVariable Integer id) {

        return ubicacionService.obtener(id);
    }

    @PostMapping
    public Ubicacion crear(
            @RequestBody Ubicacion ubicacion) {

        return ubicacionService.crear(
                ubicacion);
    }

    @PutMapping("/{id}")
    public Ubicacion actualizar(
            @PathVariable Integer id,
            @RequestBody Ubicacion ubicacion) {

        return ubicacionService.actualizar(
                id,
                ubicacion);
    }

    @DeleteMapping("/{id}")
    public String eliminar(
            @PathVariable Integer id) {

        ubicacionService.eliminar(id);

        return "Ubicación eliminada correctamente";
    }
}