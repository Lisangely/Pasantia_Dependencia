package com.fau.biblioteca_api.controller;

import com.fau.biblioteca_api.dto.LibroAutorRequest;
import com.fau.biblioteca_api.entity.LibroAutor;
import com.fau.biblioteca_api.service.LibroAutorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libro-autor")
public class LibroAutorController {

    private final LibroAutorService libroAutorService;

    public LibroAutorController(
            LibroAutorService libroAutorService) {

        this.libroAutorService = libroAutorService;
    }

    @PostMapping
    public LibroAutor asociar(
            @RequestBody LibroAutorRequest request) {

        return libroAutorService.asociar(request);
    }
}