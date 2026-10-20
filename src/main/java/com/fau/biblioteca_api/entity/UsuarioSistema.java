package com.fau.biblioteca_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "usuarios_sistema")
public class UsuarioSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private String usuario;

    @Column(unique = true)
    private String correo;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private RolUsuario rol;

    private Boolean activo = true;

    @Column(name = "puede_eliminar")
    private Boolean puedeEliminar = false;

    @Column(name = "puede_actualizar")
    private Boolean puedeActualizar = false;

    @Column(name = "puede_crear")
    private Boolean puedeCrear = false;

    @Column(name = "puede_crear_libros")
    private Boolean puedeCrearLibros = false;

    @Column(name = "puede_crear_autores")
    private Boolean puedeCrearAutores = false;

    @Column(name = "puede_crear_editoriales")
    private Boolean puedeCrearEditoriales = false;

    @Column(name = "puede_crear_ubicaciones")
    private Boolean puedeCrearUbicaciones = false;

    @Column(name = "puede_crear_ejemplares")
    private Boolean puedeCrearEjemplares = false;

    @Column(name = "puede_crear_lectores")
    private Boolean puedeCrearLectores = false;

    @Column(name = "puede_crear_prestamos")
    private Boolean puedeCrearPrestamos = false;

    @Column(name = "puede_crear_incidencias")
    private Boolean puedeCrearIncidencias = false;

    @Column(name = "pregunta_1")
    private String pregunta1;

    @Column(name = "pregunta_2")
    private String pregunta2;

    @Column(name = "pregunta_3")
    private String pregunta3;

    @JsonProperty(value = "respuesta1", access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "respuesta_1_hash")
    private String respuesta1;

    @JsonProperty(value = "respuesta2", access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "respuesta_2_hash")
    private String respuesta2;

    @JsonProperty(value = "respuesta3", access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "respuesta_3_hash")
    private String respuesta3;

    public UsuarioSistema() {
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getPuedeEliminar() { return puedeEliminar; }
    public void setPuedeEliminar(Boolean puedeEliminar) { this.puedeEliminar = puedeEliminar; }
    public Boolean getPuedeActualizar() { return puedeActualizar; }
    public void setPuedeActualizar(Boolean puedeActualizar) { this.puedeActualizar = puedeActualizar; }
    public Boolean getPuedeCrear() { return puedeCrear; }
    public void setPuedeCrear(Boolean puedeCrear) { this.puedeCrear = puedeCrear; }
    public Boolean getPuedeCrearLibros() { return puedeCrearLibros; }
    public void setPuedeCrearLibros(Boolean puedeCrearLibros) { this.puedeCrearLibros = puedeCrearLibros; }
    public Boolean getPuedeCrearAutores() { return puedeCrearAutores; }
    public void setPuedeCrearAutores(Boolean puedeCrearAutores) { this.puedeCrearAutores = puedeCrearAutores; }
    public Boolean getPuedeCrearEditoriales() { return puedeCrearEditoriales; }
    public void setPuedeCrearEditoriales(Boolean puedeCrearEditoriales) { this.puedeCrearEditoriales = puedeCrearEditoriales; }
    public Boolean getPuedeCrearUbicaciones() { return puedeCrearUbicaciones; }
    public void setPuedeCrearUbicaciones(Boolean puedeCrearUbicaciones) { this.puedeCrearUbicaciones = puedeCrearUbicaciones; }
    public Boolean getPuedeCrearEjemplares() { return puedeCrearEjemplares; }
    public void setPuedeCrearEjemplares(Boolean puedeCrearEjemplares) { this.puedeCrearEjemplares = puedeCrearEjemplares; }
    public Boolean getPuedeCrearLectores() { return puedeCrearLectores; }
    public void setPuedeCrearLectores(Boolean puedeCrearLectores) { this.puedeCrearLectores = puedeCrearLectores; }
    public Boolean getPuedeCrearPrestamos() { return puedeCrearPrestamos; }
    public void setPuedeCrearPrestamos(Boolean puedeCrearPrestamos) { this.puedeCrearPrestamos = puedeCrearPrestamos; }
    public Boolean getPuedeCrearIncidencias() { return puedeCrearIncidencias; }
    public void setPuedeCrearIncidencias(Boolean puedeCrearIncidencias) { this.puedeCrearIncidencias = puedeCrearIncidencias; }
    public String getPregunta1() { return pregunta1; }
    public void setPregunta1(String pregunta1) { this.pregunta1 = pregunta1; }
    public String getPregunta2() { return pregunta2; }
    public void setPregunta2(String pregunta2) { this.pregunta2 = pregunta2; }
    public String getPregunta3() { return pregunta3; }
    public void setPregunta3(String pregunta3) { this.pregunta3 = pregunta3; }
    public String getRespuesta1() { return respuesta1; }
    public void setRespuesta1(String respuesta1) { this.respuesta1 = respuesta1; }
    public String getRespuesta2() { return respuesta2; }
    public void setRespuesta2(String respuesta2) { this.respuesta2 = respuesta2; }
    public String getRespuesta3() { return respuesta3; }
    public void setRespuesta3(String respuesta3) { this.respuesta3 = respuesta3; }
}