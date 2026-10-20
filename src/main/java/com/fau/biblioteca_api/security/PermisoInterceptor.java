package com.fau.biblioteca_api.security;

import com.fau.biblioteca_api.service.PermisoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermisoInterceptor implements HandlerInterceptor {

    private final PermisoService permisoService;

    public PermisoInterceptor(PermisoService permisoService) {
        this.permisoService = permisoService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uri = request.getRequestURI();
        String metodo = request.getMethod();
        if (uri.startsWith("/api/usuarios")) {
            permisoService.exigirAdmin();
            return true;
        }
        if (uri.contains("/restaurar")) {
            permisoService.exigirAdmin("Solo el administrador puede devolver una obra a una versión anterior");
            return true;
        }
        if (uri.startsWith("/api/auth") || "GET".equals(metodo) || "OPTIONS".equals(metodo)) {
            return true;
        }
        String accion = accion(metodo, uri);
        if (accion != null) {
            permisoService.exigir(accion);
        }
        return true;
    }

    private String accion(String metodo, String uri) {
        if ("DELETE".equals(metodo)) {
            return "eliminar";
        }
        if ("PUT".equals(metodo)) {
            return "actualizar";
        }
        if (!"POST".equals(metodo)) {
            return null;
        }
        if (uri.startsWith("/api/libros")) return "libros";
        if (uri.startsWith("/api/autores") || uri.startsWith("/api/libro-autor")) return "autores";
        if (uri.startsWith("/api/editoriales")) return "editoriales";
        if (uri.startsWith("/api/ubicaciones")) return "ubicaciones";
        if (uri.startsWith("/api/ejemplares")) return "ejemplares";
        if (uri.startsWith("/api/lectores")) return "lectores";
        if (uri.startsWith("/api/prestamos")) return "prestamos";
        if (uri.startsWith("/api/incidencias")) return "incidencias";
        return null;
    }
}
