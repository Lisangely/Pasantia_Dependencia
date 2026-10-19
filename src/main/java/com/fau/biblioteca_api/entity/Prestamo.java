package com.fau.biblioteca_api.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "prestamos")
public class Prestamo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prestamo")
    private Integer idPrestamo;

    @ManyToOne
    @JoinColumn(name = "id_ejemplar")
    private Ejemplar ejemplar;

    @ManyToOne
    @JoinColumn(name = "id_lector")
    private Lector lector;

    @ManyToOne
    @JoinColumn(name = "registrado_por")
    private UsuarioSistema registradoPor;

    @Column(name = "fecha_prestamo")
    private LocalDateTime fechaPrestamo;

    @Column(name = "fecha_devolucion_esperada")
    private LocalDateTime fechaDevolucionEsperada;

    @Column(name = "fecha_devolucion_real")
    private LocalDateTime fechaDevolucionReal;

    @Enumerated(EnumType.STRING)
    private EstadoPrestamo estado;

    public Prestamo() {
    }

    public Integer getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Integer idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
    }

    public Lector getLector() {
        return lector;
    }

    public void setLector(Lector lector) {
        this.lector = lector;
    }

    public UsuarioSistema getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(
            UsuarioSistema registradoPor) {

        this.registradoPor = registradoPor;
    }

    public LocalDateTime getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(
            LocalDateTime fechaPrestamo) {

        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDateTime getFechaDevolucionEsperada() {
        return fechaDevolucionEsperada;
    }

    public void setFechaDevolucionEsperada(
            LocalDateTime fechaDevolucionEsperada) {

        this.fechaDevolucionEsperada =
                fechaDevolucionEsperada;
    }

    public LocalDateTime getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(
            LocalDateTime fechaDevolucionReal) {

        this.fechaDevolucionReal =
                fechaDevolucionReal;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(
            EstadoPrestamo estado) {

        this.estado = estado;
    }
}