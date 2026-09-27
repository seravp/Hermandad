package com.hermandad.controller;

import com.hermandad.dto.AuditoriaResponseDto;
import com.hermandad.mapper.AuditoriaMapper;
import com.hermandad.service.AuditoriaService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    private final AuditoriaMapper auditoriaMapper;

    public AuditoriaController(
            AuditoriaService auditoriaService,
            AuditoriaMapper auditoriaMapper) {

        this.auditoriaService = auditoriaService;
        this.auditoriaMapper = auditoriaMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditoriaResponseDto> buscar(

            @RequestParam(required = false)
            String usuario,

            @RequestParam(required = false)
            String accion,

            @RequestParam(required = false)
            String entidad,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size) {

        return auditoriaService
                .buscarPaginado(
                        usuario,
                        accion,
                        entidad,
                        page,
                        size)
                .map(auditoriaMapper::toResponse);
    }


}