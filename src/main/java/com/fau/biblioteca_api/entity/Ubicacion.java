package com.fau.biblioteca_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ubicaciones")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer idUbicacion;

    @Column(name = "ubicacion_original", nullable = false)
    private String ubicacionOriginal;

    private String estanteria;

    private String posicion;

    @Column(name = "requiere_revision")
    private Boolean requiereRevision = false;

    public Ubicacion() {
    }

    public Integer getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Integer idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public String getUbicacionOriginal() {
        return ubicacionOriginal;
    }

    public void setUbicacionOriginal(String ubicacionOriginal) {
        this.ubicacionOriginal = ubicacionOriginal;
    }

    public String getEstanteria() {
        return estanteria;
    }

    public void setEstanteria(String estanteria) {
        this.estanteria = estanteria;
    }

    public String getPosicion() {
        return posicion;
    }

    public void setPosicion(String posicion) {
        this.posicion = posicion;
    }

    public Boolean getRequiereRevision() {
        return requiereRevision;
    }

    public void setRequiereRevision(Boolean requiereRevision) {
        this.requiereRevision = requiereRevision;
    }
}