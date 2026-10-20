package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.repository.UsuarioSistemaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioActualService {

    private final UsuarioSistemaRepository usuarioRepository;

    public UsuarioActualService(UsuarioSistemaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioSistema obtener() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {

            throw new RuntimeException("No hay un usuario autenticado");
        }

        return usuarioRepository.findByUsuario(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Usuario autenticado no encontrado"));
    }
}
