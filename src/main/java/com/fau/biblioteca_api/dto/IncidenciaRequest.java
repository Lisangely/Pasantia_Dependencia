package com.fau.biblioteca_api.dto;

public class IncidenciaRequest {

    private Integer idLibro;
    private String campo;
    private String valorOriginal;
    private String descripcion;

    public IncidenciaRequest() {
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public String getCampo() {
        return campo;
    }

    public void setCampo(String campo) {
        this.campo = campo;
    }

    public String getValorOriginal() {
        return valorOriginal;
    }

    public void setValorOriginal(String valorOriginal) {
        this.valorOriginal = valorOriginal;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
