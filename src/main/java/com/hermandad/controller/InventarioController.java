package com.hermandad.controller;

import com.hermandad.dto.*;
import com.hermandad.entity.EstadoInventario;
import com.hermandad.mapper.InventarioMapper;
import com.hermandad.service.InventarioService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventario")
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioMapper inventarioMapper;

    public InventarioController(InventarioService inventarioService, InventarioMapper inventarioMapper) {
        this.inventarioService = inventarioService;
        this.inventarioMapper = inventarioMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','TESORERO','CONSULTA')")
    public Page<InventarioResponseDto> buscar(
            @RequestParam(defaultValue = "") String texto,
            @RequestParam(defaultValue = "") String categoria,
            @RequestParam(required = false) EstadoInventario estado,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return inventarioService.buscar(texto, categoria, estado,
                        PageRequest.of(page, size, Sort.by("codigo").ascending()))
                .map(inventarioMapper::toResponse);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','TESORERO','CONSULTA')")
    public InventarioResponseDto obtener(@PathVariable Long id) {
        return inventarioMapper.toResponse(inventarioService.obtenerElemento(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public InventarioResponseDto crear(@Valid @RequestBody InventarioRequestDto dto) {
        return inventarioMapper.toResponse(inventarioService.crearElemento(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public InventarioResponseDto actualizar(@PathVariable Long id, @Valid @RequestBody InventarioRequestDto dto) {
        return inventarioMapper.toResponse(inventarioService.actualizarElemento(id, dto));
    }

    @PutMapping("/{id}/activo")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public InventarioResponseDto cambiarActivo(@PathVariable Long id, @RequestParam boolean activo) {
        return inventarioMapper.toResponse(inventarioService.cambiarActivo(id, activo));
    }

    @GetMapping("/revisiones")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','TESORERO','CONSULTA')")
    public List<RevisionInventarioResponseDto> revisiones() {
        return inventarioService.obtenerRevisiones().stream()
                .map(revision -> inventarioMapper.toRevisionResponse(revision,
                        inventarioService.totalDetalles(revision.getId()),
                        inventarioService.totalVerificados(revision.getId()),
                        inventarioService.totalIncidencias(revision.getId())))
                .toList();
    }

    @PostMapping("/revisiones")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public RevisionInventarioResponseDto crearRevision(@Valid @RequestBody RevisionInventarioRequestDto dto) {
        var revision = inventarioService.crearRevision(dto);
        return inventarioMapper.toRevisionResponse(revision,
                inventarioService.totalDetalles(revision.getId()), 0, 0);
    }

    @GetMapping("/revisiones/{id}/detalles")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO','TESORERO','CONSULTA')")
    public List<RevisionInventarioDetalleResponseDto> detalles(@PathVariable Long id) {
        return inventarioService.obtenerDetalles(id).stream()
                .map(inventarioMapper::toDetalleResponse)
                .toList();
    }

    @PutMapping("/revisiones/detalles/{detalleId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public RevisionInventarioDetalleResponseDto actualizarDetalle(
            @PathVariable Long detalleId,
            @Valid @RequestBody RevisionInventarioDetalleRequestDto dto) {
        return inventarioMapper.toDetalleResponse(inventarioService.actualizarDetalle(detalleId, dto));
    }

    @PutMapping("/revisiones/{id}/cerrar")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public RevisionInventarioResponseDto cerrarRevision(@PathVariable Long id) {
        var revision = inventarioService.cerrarRevision(id);
        return inventarioMapper.toRevisionResponse(revision,
                inventarioService.totalDetalles(id),
                inventarioService.totalVerificados(id),
                inventarioService.totalIncidencias(id));
    }
}
