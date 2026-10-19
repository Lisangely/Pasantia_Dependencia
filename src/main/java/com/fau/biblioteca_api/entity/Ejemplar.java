package com.fau.biblioteca_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ejemplares")
public class Ejemplar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ejemplar")
    private Integer idEjemplar;

    @ManyToOne
    @JoinColumn(name = "id_libro")
    private Libro libro;

    @ManyToOne
    @JoinColumn(name = "id_ubicacion")
    private Ubicacion ubicacion;

    @Column(name = "numero_ejemplar")
    private Integer numeroEjemplar;

    private Boolean disponible = true;

    @Column(name = "requiere_revision")
    private Boolean requiereRevision = false;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    public Ejemplar() {
    }

    public Integer getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Integer idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}