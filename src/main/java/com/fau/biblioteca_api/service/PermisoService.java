package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.RolUsuario;
import com.fau.biblioteca_api.entity.UsuarioSistema;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PermisoService {

    private final UsuarioActualService usuarioActualService;

    public PermisoService(UsuarioActualService usuarioActualService) {
        this.usuarioActualService = usuarioActualService;
    }

    public void exigirAdmin() {
        exigirAdmin("Solo el administrador puede gestionar usuarios");
    }

    public void exigirAdmin(String mensaje) {
        if (!esAdmin(usuarioActualService.obtener())) {
            throw prohibido(mensaje);
        }
    }

    public void exigir(String accion) {
        UsuarioSistema usuario = usuarioActualService.obtener();
        if (esAdmin(usuario)) {
            return;
        }
        boolean permitido = switch (accion) {
            case "eliminar" -> verdad(usuario.getPuedeEliminar());
            case "actualizar" -> verdad(usuario.getPuedeActualizar());
            case "libros" -> puedeCrear(usuario, usuario.getPuedeCrearLibros());
            case "autores" -> puedeCrear(usuario, usuario.getPuedeCrearAutores());
            case "editoriales" -> puedeCrear(usuario, usuario.getPuedeCrearEditoriales());
            case "ubicaciones" -> puedeCrear(usuario, usuario.getPuedeCrearUbicaciones());
            case "ejemplares" -> puedeCrear(usuario, usuario.getPuedeCrearEjemplares());
            case "lectores" -> puedeCrear(usuario, usuario.getPuedeCrearLectores());
            case "prestamos" -> puedeCrear(usuario, usuario.getPuedeCrearPrestamos());
            case "incidencias" -> puedeCrear(usuario, usuario.getPuedeCrearIncidencias());
            default -> false;
        };
        if (!permitido) {
            throw prohibido("No tienes permiso para esta acción");
        }
    }

    private boolean puedeCrear(UsuarioSistema usuario, Boolean especifico) {
        return verdad(usuario.getPuedeCrear()) && verdad(especifico);
    }

    private boolean esAdmin(UsuarioSistema usuario) {
        return usuario.getRol() == RolUsuario.administrador;
    }

    private boolean verdad(Boolean valor) {
        return Boolean.TRUE.equals(valor);
    }

    private ResponseStatusException prohibido(String mensaje) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, mensaje);
    }
}
