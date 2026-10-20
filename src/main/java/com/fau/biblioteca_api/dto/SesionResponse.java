package com.fau.biblioteca_api.dto;

import com.fau.biblioteca_api.entity.UsuarioSistema;

public class SesionResponse {

    private Integer idUsuario;
    private String nombre;
    private String apellido;
    private String usuario;
    private String correo;
    private String rol;
    private Boolean puedeEliminar;
    private Boolean puedeActualizar;
    private Boolean puedeCrear;
    private Boolean puedeCrearLibros;
    private Boolean puedeCrearAutores;
    private Boolean puedeCrearEditoriales;
    private Boolean puedeCrearUbicaciones;
    private Boolean puedeCrearEjemplares;
    private Boolean puedeCrearLectores;
    private Boolean puedeCrearPrestamos;
    private Boolean puedeCrearIncidencias;

    public SesionResponse() {
    }

    public static SesionResponse desde(UsuarioSistema usuario) {
        SesionResponse respuesta = new SesionResponse();
        respuesta.idUsuario = usuario.getIdUsuario();
        respuesta.nombre = usuario.getNombre();
        respuesta.apellido = usuario.getApellido();
        respuesta.usuario = usuario.getUsuario();
        respuesta.correo = usuario.getCorreo();
        respuesta.rol = usuario.getRol() == null ? null : usuario.getRol().name();
        boolean admin = usuario.getRol() != null && usuario.getRol().name().equals("administrador");
        respuesta.puedeEliminar = admin || Boolean.TRUE.equals(usuario.getPuedeEliminar());
        respuesta.puedeActualizar = admin || Boolean.TRUE.equals(usuario.getPuedeActualizar());
        respuesta.puedeCrear = admin || Boolean.TRUE.equals(usuario.getPuedeCrear());
        respuesta.puedeCrearLibros = admin || Boolean.TRUE.equals(usuario.getPuedeCrearLibros());
        respuesta.puedeCrearAutores = admin || Boolean.TRUE.equals(usuario.getPuedeCrearAutores());
        respuesta.puedeCrearEditoriales = admin || Boolean.TRUE.equals(usuario.getPuedeCrearEditoriales());
        respuesta.puedeCrearUbicaciones = admin || Boolean.TRUE.equals(usuario.getPuedeCrearUbicaciones());
        respuesta.puedeCrearEjemplares = admin || Boolean.TRUE.equals(usuario.getPuedeCrearEjemplares());
        respuesta.puedeCrearLectores = admin || Boolean.TRUE.equals(usuario.getPuedeCrearLectores());
        respuesta.puedeCrearPrestamos = admin || Boolean.TRUE.equals(usuario.getPuedeCrearPrestamos());
        respuesta.puedeCrearIncidencias = admin || Boolean.TRUE.equals(usuario.getPuedeCrearIncidencias());
        return respuesta;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public String getRol() { return rol; }
    public Boolean getPuedeEliminar() { return puedeEliminar; }
    public Boolean getPuedeActualizar() { return puedeActualizar; }
    public Boolean getPuedeCrear() { return puedeCrear; }
    public Boolean getPuedeCrearLibros() { return puedeCrearLibros; }
    public Boolean getPuedeCrearAutores() { return puedeCrearAutores; }
    public Boolean getPuedeCrearEditoriales() { return puedeCrearEditoriales; }
    public Boolean getPuedeCrearUbicaciones() { return puedeCrearUbicaciones; }
    public Boolean getPuedeCrearEjemplares() { return puedeCrearEjemplares; }
    public Boolean getPuedeCrearLectores() { return puedeCrearLectores; }
    public Boolean getPuedeCrearPrestamos() { return puedeCrearPrestamos; }
    public Boolean getPuedeCrearIncidencias() { return puedeCrearIncidencias; }
}
