package com.fau.biblioteca_api.entity;

import java.io.Serializable;
import java.util.Objects;

public class LibroAutorId implements Serializable {

    private Integer libro;
    private Integer autor;

    public LibroAutorId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LibroAutorId)) return false;

        LibroAutorId that = (LibroAutorId) o;

        return Objects.equals(libro, that.libro)
                && Objects.equals(autor, that.autor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libro, autor);
    }
}