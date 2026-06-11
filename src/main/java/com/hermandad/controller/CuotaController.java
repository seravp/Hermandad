package com.hermandad.controller;

import com.hermandad.dto.CuotaRequestDto;
import com.hermandad.dto.CuotaResponseDto;
import com.hermandad.dto.MorosoDto;
import com.hermandad.dto.ResumenCuotasDto;
import com.hermandad.entity.Cuota;
import com.hermandad.mapper.CuotaMapper;
import com.hermandad.service.CuotaService;
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
    public List<CuotaResponseDto> obtenerPorHermano(
            @PathVariable Long id) {

        return cuotaService
                .obtenerPorHermano(id)
                .stream()
                .map(cuotaMapper::toResponse)
                .toList();
    }

    @PostMapping
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
    public CuotaResponseDto pagar(
            @PathVariable Long id) {

        Cuota cuota = cuotaService.pagar(id);

        return cuotaMapper.toResponse(cuota);
    }

    @PutMapping("/{id}/anular")
    public CuotaResponseDto anular(
            @PathVariable Long id) {

        Cuota cuota = cuotaService.anular(id);

        return cuotaMapper.toResponse(cuota);
    }

    @GetMapping("/pendientes")
    public List<CuotaResponseDto> pendientes() {

        return cuotaService
                .obtenerPendientes()
                .stream()
                .map(cuotaMapper::toResponse)
                .toList();
    }

    @GetMapping("/resumen")
    public ResumenCuotasDto resumen() {

        return cuotaService.obtenerResumen();
    }

    @GetMapping("/morosos")
    public List<MorosoDto> morosos() {

        return cuotaService.obtenerMorosos();
    }
}