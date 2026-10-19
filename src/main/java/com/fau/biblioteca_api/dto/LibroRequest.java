package com.fau.biblioteca_api.dto;

public class LibroRequest {

    private String titulo;

    private String tipo;

    private Short fechaPublicacion;

    private String fechaOriginal;

    private Boolean requiereRevisionFecha;

    private String cota;

    private String observaciones;

    private Integer idEditorial;

    public LibroRequest() {}

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Short getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(Short fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public String getFechaOriginal() {
        return fechaOriginal;
    }

    public void setFechaOriginal(String fechaOriginal) {
        this.fechaOriginal = fechaOriginal;
    }

    public Boolean getRequiereRevisionFecha() {
        return requiereRevisionFecha;
    }

    public void setRequiereRevisionFecha(Boolean requiereRevisionFecha) {
        this.requiereRevisionFecha = requiereRevisionFecha;
    }

    public String getCota() {
        return cota;
    }

    public void setCota(String cota) {
        this.cota = cota;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Integer getIdEditorial() {
        return idEditorial;
    }

    public void setIdEditorial(Integer idEditorial) {
        this.idEditorial = idEditorial;
    }
}