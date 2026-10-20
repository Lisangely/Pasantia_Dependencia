package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.dto.AutorLibroResumen;
import com.fau.biblioteca_api.dto.LibroAutorRequest;
import com.fau.biblioteca_api.entity.LibroAutor;
import com.fau.biblioteca_api.service.LibroAutorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libro-autor")
public class LibroAutorController {

    private final LibroAutorService libroAutorService;

    public LibroAutorController(LibroAutorService libroAutorService) {
        this.libroAutorService = libroAutorService;
    }

    @GetMapping("/resumen")
    public List<AutorLibroResumen> resumen() {
        return libroAutorService.resumen();
    }

    @GetMapping
    public List<LibroAutor> listar(
            @RequestParam(required = false) Integer idLibro) {

        return libroAutorService.listar(idLibro);
    }

    @PostMapping
    public LibroAutor asociar(@RequestBody LibroAutorRequest request) {
        return libroAutorService.asociar(request);
    }

    @DeleteMapping
    public String eliminar(
            @RequestParam Integer idLibro,
            @RequestParam Integer idAutor) {

        libroAutorService.eliminar(idLibro, idAutor);
        return "Relación libro-autor eliminada correctamente";
    }
}
