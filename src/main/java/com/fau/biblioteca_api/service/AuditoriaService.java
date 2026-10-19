package com.fau.biblioteca_api.service;

import com.fau.biblioteca_api.entity.*;
import com.fau.biblioteca_api.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(
            AuditoriaRepository auditoriaRepository) {

        this.auditoriaRepository = auditoriaRepository;
    }

    public List<Auditoria> listar() {
        return auditoriaRepository.findAll();
    }

    public Auditoria obtener(Integer id) {

        return auditoriaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Auditoría no encontrada"));
    }

    public void registrar(
            UsuarioSistema usuario,
            String entidad,
            Integer idEntidad,
            AccionAuditoria accion,
            String valorAnterior,
            String valorNuevo,
            String observaciones) {

        Auditoria auditoria =
                new Auditoria();

        auditoria.setUsuario(usuario);

        auditoria.setFechaEvento(
                LocalDateTime.now());

        auditoria.setEntidad(entidad);

        auditoria.setIdEntidad(idEntidad);

        auditoria.setAccion(accion);

        auditoria.setValorAnterior(
                valorAnterior);

        auditoria.setValorNuevo(
                valorNuevo);

        auditoria.setObservaciones(
                observaciones);

        auditoriaRepository.save(auditoria);
    }
}