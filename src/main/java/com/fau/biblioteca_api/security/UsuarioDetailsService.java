package com.fau.biblioteca_api.security;

import com.fau.biblioteca_api.entity.UsuarioSistema;
import com.fau.biblioteca_api.repository.UsuarioSistemaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioSistemaRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioSistemaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        UsuarioSistema usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado"));

        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("El usuario no tiene rol");
        }

        return User.builder()
                .username(usuario.getUsuario())
                .password(usuario.getPasswordHash())
                .roles(usuario.getRol().name())
                .disabled(!Boolean.TRUE.equals(usuario.getActivo()))
                .build();
    }
}
