package com.hermandad.controller;

import com.hermandad.entity.EstadoHermano;
import com.hermandad.entity.Hermano;
import com.hermandad.service.HermanoService;

import com.hermandad.dto.HermanoRequestDto;
import com.hermandad.dto.HermanoResponseDto;
import com.hermandad.mapper.HermanoMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Tag(name = "Hermanos")
@RestController
@RequestMapping("/api/hermanos")
public class HermanoController {
    private final HermanoMapper hermanoMapper;
    private final HermanoService hermanoService;

    public HermanoController(
            HermanoService hermanoService,
            HermanoMapper hermanoMapper) {

        this.hermanoService = hermanoService;
        this.hermanoMapper = hermanoMapper;
    }

    @Operation(summary = "Obtener todos los hermanos")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public List<HermanoResponseDto> obtenerTodos() {

        return hermanoService.obtenerTodos()
                .stream()
                .map(hermanoMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Obtener hermano por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public HermanoResponseDto obtenerPorId(
            @PathVariable Long id) {

        Hermano hermano = hermanoService.obtenerPorId(id);

        return hermanoMapper.toResponse(hermano);
    }

    @Operation(summary = "Obtener hermano por DNI")
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public HermanoResponseDto buscarPorDni(
            @PathVariable String dni) {

        Hermano hermano = hermanoService.buscarPorDni(dni);

        return hermanoMapper.toResponse(hermano);
    }

    @Operation(summary = "Obtener hermano por estado")
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public List<HermanoResponseDto> buscarPorEstado(
            @PathVariable EstadoHermano estado) {

        return hermanoService.buscarPorEstado(estado)
                .stream()
                .map(hermanoMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Obtener hermano por apellidos")
    @GetMapping("/apellidos/{apellidos}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public List<HermanoResponseDto> buscarPorApellidos(
            @PathVariable String apellidos) {

        return hermanoService.buscarPorApellidos(apellidos)
                .stream()
                .map(hermanoMapper::toResponse)
                .toList();
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public Page<HermanoResponseDto> obtenerPaginados(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "numeroHermano")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return hermanoService
                .obtenerPaginados(
                        page,
                        size,
                        sort,
                        direction)
                .map(hermanoMapper::toResponse);
    }

    @GetMapping("/busqueda-paginada")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public Page<HermanoResponseDto> buscarPaginado(

            @RequestParam(required = false)
            String nombre,

            @RequestParam(required = false)
            String apellidos,

            @RequestParam(required = false)
            String dni,

            @RequestParam(required = false)
            EstadoHermano estado,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "numeroHermano")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return hermanoService
                .buscarPaginado(
                        nombre,
                        apellidos,
                        dni,
                        estado,
                        page,
                        size,
                        sort,
                        direction)
                .map(hermanoMapper::toResponse);
    }

    @GetMapping("/domiciliados")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','CONSULTA')")
    public List<HermanoResponseDto> domiciliados() {

        return hermanoService
                .obtenerDomiciliados()
                .stream()
                .map(hermanoMapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public HermanoResponseDto crear(
            @Valid @RequestBody HermanoRequestDto dto) {

        Hermano hermano = hermanoMapper.toEntity(dto);

        Hermano guardado = hermanoService.guardar(hermano);

        return hermanoMapper.toResponse(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public HermanoResponseDto actualizar(
            @PathVariable Long id,
            @Valid @RequestBody HermanoRequestDto dto) {

        Hermano hermano = hermanoMapper.toEntity(dto);

        Hermano actualizado =
                hermanoService.actualizar(id, hermano);

        return hermanoMapper.toResponse(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminar(@PathVariable Long id) {

        hermanoService.eliminar(id);
    }
}