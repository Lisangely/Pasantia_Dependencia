package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.UsuarioSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioSistemaRepository
        extends JpaRepository<UsuarioSistema, Integer> {

    Optional<UsuarioSistema> findByUsuario(String usuario);
}