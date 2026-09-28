package com.hermandad.controller;

import com.hermandad.dto.ConfiguracionDto;
import com.hermandad.dto.ConfiguracionCuotasDto;
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

        ConfiguracionDto dto = configuracionMapper.toResponse(configuracion);
        dto.setCuadrillas(configuracionService.obtenerCuadrillas());
        return dto;
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ConfiguracionDto actualizar(
            @RequestBody ConfiguracionDto dto) {

        Configuracion configuracion =
                configuracionService.actualizar(dto);

        ConfiguracionDto respuesta = configuracionMapper.toResponse(configuracion);
        respuesta.setCuadrillas(configuracionService.obtenerCuadrillas());
        return respuesta;
    }

    @GetMapping("/cuotas")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO')")
    public ConfiguracionCuotasDto obtenerConfiguracionCuotas() {
        Configuracion configuracion = configuracionService.obtenerConfiguracion();
        return new ConfiguracionCuotasDto(
                configuracion.getAnioActivo(),
                configuracion.getImporteCuotaHermano(),
                configuracion.getImporteCuotaCostalero());
    }

    @GetMapping("/cuadrillas/{nombre}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public com.hermandad.dto.ConfiguracionCuadrillaDto obtenerCuadrilla(@PathVariable String nombre) {
        return configuracionService.obtenerCuadrilla(nombre);
    }

    @PutMapping("/cuadrillas")
    @PreAuthorize("hasRole('ADMIN')")
    public java.util.List<com.hermandad.dto.ConfiguracionCuadrillaDto> actualizarCuadrillas(
            @RequestBody java.util.List<com.hermandad.dto.ConfiguracionCuadrillaDto> cuadrillas) {
        configuracionService.actualizarCuadrillas(cuadrillas);
        return configuracionService.obtenerCuadrillas();
    }

}
