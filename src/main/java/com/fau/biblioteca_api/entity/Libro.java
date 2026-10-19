package com.fau.biblioteca_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "libros")
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_libro")
    private Integer idLibro;

    @Column(nullable = false, length = 500)
    private String titulo;

    private String tipo;

    @Column(name = "fecha_publicacion")
    private Short fechaPublicacion;

    @Column(name = "fecha_original")
    private String fechaOriginal;

    @Column(name = "requiere_revision_fecha")
    private Boolean requiereRevisionFecha = false;

    private String cota;

    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "id_editorial")
    private Editorial editorial;

    public Libro() {}

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

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

    public Editorial getEditorial() {
        return editorial;
    }

    public void setEditorial(Editorial editorial) {
        this.editorial = editorial;
    }
}