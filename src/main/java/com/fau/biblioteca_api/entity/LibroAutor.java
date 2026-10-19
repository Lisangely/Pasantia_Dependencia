package com.fau.biblioteca_api.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "libro_autor")
@IdClass(LibroAutorId.class)
public class LibroAutor {

    @Id
    @ManyToOne
    @JoinColumn(name = "id_libro")
    private Libro libro;

    @Id
    @ManyToOne
    @JoinColumn(name = "id_autor")
    private Autor autor;

    public LibroAutor() {
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public Autor getAutor() {
        return autor;
    }

    public void setAutor(Autor autor) {
        this.autor = autor;
    }
}