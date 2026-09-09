package com.hermandad.controller;

import com.hermandad.dto.*;
import com.hermandad.entity.Cuota;
import com.hermandad.entity.EstadoCuota;
import com.hermandad.mapper.CuotaMapper;
import com.hermandad.service.CuotaService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaService cuotaService;
    private final CuotaMapper cuotaMapper;

    public CuotaController(
            CuotaService cuotaService,
            CuotaMapper cuotaMapper) {

        this.cuotaService = cuotaService;
        this.cuotaMapper = cuotaMapper;
    }

    @GetMapping("/hermano/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','CONSULTA')")
    public List<CuotaResponseDto> obtenerPorHermano(
            @PathVariable Long id) {

        return cuotaService
                .obtenerPorHermano(id)
                .stream()
                .map(cuotaMapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public CuotaResponseDto crear(
            @RequestBody CuotaRequestDto dto) {

        Cuota cuota =
                cuotaMapper.toEntity(dto);

        Cuota guardada =
                cuotaService.guardar(
                        dto.getHermanoId(),
                        cuota);

        return cuotaMapper.toResponse(
                guardada);
    }

    @PostMapping("/generar/{anio}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public String generarCuotas(

            @PathVariable Integer anio,

            @RequestParam BigDecimal importe) {

        int total =
                cuotaService.generarCuotasAnuales(
                        anio,
                        importe);

        return "Se han generado "
                + total
                + " cuotas";
    }

    @PutMapping("/{id}/pagar")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public CuotaResponseDto pagar(
            @PathVariable Long id) {

        Cuota cuota = cuotaService.pagar(id);

        return cuotaMapper.toResponse(cuota);
    }

    @PutMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public CuotaResponseDto anular(
            @PathVariable Long id) {

        Cuota cuota = cuotaService.anular(id);

        return cuotaMapper.toResponse(cuota);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public CuotaResponseDto actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CuotaRequestDto dto) {

        Cuota cuota = cuotaMapper.toEntity(dto);

        return cuotaMapper.toResponse(
                cuotaService.actualizar(id, cuota));
    }

    @GetMapping("/pendientes")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public List<CuotaResponseDto> pendientes() {

        return cuotaService
                .obtenerPendientes()
                .stream()
                .map(cuotaMapper::toResponse)
                .toList();
    }

    @GetMapping("/resumen")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResumenCuotasDto resumen() {

        return cuotaService.obtenerResumen();
    }

    @GetMapping("/morosos")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public List<MorosoDto> morosos() {

        return cuotaService.obtenerMorosos();
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public DashboardTesoreriaDto dashboard() {

        return cuotaService.obtenerDashboard();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','CONSULTA')")
    public CuotaResponseDto obtenerPorId(
            @PathVariable Long id) {

        return cuotaMapper.toResponse(
                cuotaService.obtenerPorId(id));
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','CONSULTA')")
    public Page<CuotaResponseDto> obtenerPaginadas(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "anio")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return cuotaService
                .obtenerPaginadas(
                        page,
                        size,
                        sort,
                        direction)
                .map(cuotaMapper::toResponse);
    }

    @GetMapping("/busqueda-paginada")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','CONSULTA')")
    public Page<CuotaResponseDto> buscarPaginado(

            @RequestParam(required = false)
            String texto,

            @RequestParam(required = false)
            EstadoCuota estado,

            @RequestParam(required = false)
            Integer anio,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "anio")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return cuotaService
                .buscarPaginado(
                        texto,
                        estado,
                        anio,
                        page,
                        size,
                        sort,
                        direction)
                .map(cuotaMapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {

        cuotaService.eliminar(id);
    }

    @GetMapping("/anios")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','CONSULTA')")
    public List<Integer> obtenerAniosDisponibles() {
        return cuotaService.obtenerAniosDisponibles();
    }

}