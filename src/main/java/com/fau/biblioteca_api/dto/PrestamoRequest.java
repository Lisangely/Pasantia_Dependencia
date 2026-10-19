package com.fau.biblioteca_api.dto;

import java.time.LocalDateTime;

public class PrestamoRequest {

    private Integer idEjemplar;

    private Integer idLector;

    private Integer registradoPor;

    private LocalDateTime fechaDevolucionEsperada;

    public PrestamoRequest() {
    }

    public Integer getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Integer idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public Integer getIdLector() {
        return idLector;
    }

    public void setIdLector(Integer idLector) {
        this.idLector = idLector;
    }

    public Integer getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(Integer registradoPor) {
        this.registradoPor = registradoPor;
    }

    public LocalDateTime getFechaDevolucionEsperada() {
        return fechaDevolucionEsperada;
    }

    public void setFechaDevolucionEsperada(
            LocalDateTime fechaDevolucionEsperada) {

        this.fechaDevolucionEsperada =
                fechaDevolucionEsperada;
    }
}