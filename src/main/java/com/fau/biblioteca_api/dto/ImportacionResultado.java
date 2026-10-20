package com.fau.biblioteca_api.dto;

public class ImportacionResultado {

    private int cargadas;
    private int ignoradas;

    public int getCargadas() {
        return cargadas;
    }

    public int getIgnoradas() {
        return ignoradas;
    }

    public void sumarCargada() {
        cargadas++;
    }

    public void sumarIgnorada() {
        ignoradas++;
    }
}
