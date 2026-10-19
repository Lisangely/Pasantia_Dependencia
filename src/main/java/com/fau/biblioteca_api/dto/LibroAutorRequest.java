package com.fau.biblioteca_api.dto;

public class LibroAutorRequest {

    private Integer idLibro;

    private Integer idAutor;

    public LibroAutorRequest() {
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public Integer getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(Integer idAutor) {
        this.idAutor = idAutor;
    }
}