package com.hermandad.service;

import com.hermandad.entity.Auditoria;
import com.hermandad.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    private final AuditoriaRepository
            auditoriaRepository;

    public AuditoriaService(
            AuditoriaRepository auditoriaRepository) {

        this.auditoriaRepository =
                auditoriaRepository;
    }

    public void registrar(
            String accion,
            String entidad,
            Long registroId) {

        Authentication auth =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String usuario =
                auth != null
                        ? auth.getName()
                        : "SISTEMA";

        Auditoria auditoria =
                new Auditoria();

        auditoria.setUsuario(usuario);
        auditoria.setAccion(accion);
        auditoria.setEntidad(entidad);
        auditoria.setRegistroId(registroId);
        auditoria.setFecha(
                LocalDateTime.now());

        auditoriaRepository.save(
                auditoria);
    }

    public List<Auditoria> obtenerTodas() {

        return auditoriaRepository.findAll();
    }

    public Page<Auditoria> buscarPaginado(
            String usuario,
            String accion,
            String entidad,
            int page,
            int size) {

        if (usuario == null) {
            usuario = "";
        }

        if (accion == null) {
            accion = "";
        }

        if (entidad == null) {
            entidad = "";
        }

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                "fecha"));

        return auditoriaRepository
                .findByUsuarioContainingIgnoreCaseAndAccionContainingIgnoreCaseAndEntidadContainingIgnoreCase(
                        usuario,
                        accion,
                        entidad,
                        pageable);
    }
}