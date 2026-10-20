package com.fau.biblioteca_api.dto;

public class AutorLibroResumen {

    private Integer idLibro;
    private String nombre;
    private String apellido;
    private String nombreCompleto;

    public AutorLibroResumen(Integer idLibro, String nombre, String apellido, String nombreCompleto) {
        this.idLibro = idLibro;
        this.nombre = nombre;
        this.apellido = apellido;
        this.nombreCompleto = nombreCompleto;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }
}
