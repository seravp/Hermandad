package com.hermandad.controller;

import com.hermandad.dto.ConfiguracionDto;
import com.hermandad.entity.Configuracion;
import com.hermandad.mapper.ConfiguracionMapper;
import com.hermandad.service.ConfiguracionService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/configuracion")
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;
    private final ConfiguracionMapper configuracionMapper;

    public ConfiguracionController(
            ConfiguracionService configuracionService,
            ConfiguracionMapper configuracionMapper) {

        this.configuracionService = configuracionService;
        this.configuracionMapper = configuracionMapper;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionDto obtener() {

        Configuracion configuracion =
                configuracionService.obtenerConfiguracion();

        return configuracionMapper.toResponse(configuracion);
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionDto actualizar(
            @RequestBody ConfiguracionDto dto) {

        Configuracion configuracion =
                configuracionService.actualizar(dto);

        return configuracionMapper.toResponse(configuracion);
    }

}