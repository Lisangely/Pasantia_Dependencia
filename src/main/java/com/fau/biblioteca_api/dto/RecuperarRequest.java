package com.fau.biblioteca_api.dto;

import java.util.List;

public class RecuperarRequest {

    private String usuario;
    private List<String> respuestas;
    private String password;

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public List<String> getRespuestas() {
        return respuestas;
    }

    public void setRespuestas(List<String> respuestas) {
        this.respuestas = respuestas;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
