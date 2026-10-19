package com.fau.biblioteca_api.dto;

public class EjemplarRequest {

    private Integer idLibro;

    private Integer idUbicacion;

    private Integer numeroEjemplar;

    private Boolean disponible;

    private Boolean requiereRevision;

    public EjemplarRequest() {
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public Integer getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Integer idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public Integer getNumeroEjemplar() {
        return numeroEjemplar;
    }

    public void setNumeroEjemplar(Integer numeroEjemplar) {
        this.numeroEjemplar = numeroEjemplar;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Boolean getRequiereRevision() {
        return requiereRevision;
    }

    public void setRequiereRevision(Boolean requiereRevision) {
        this.requiereRevision = requiereRevision;
    }
}