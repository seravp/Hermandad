package com.hermandad.controller;

import com.hermandad.dto.CuotaRequestDto;
import com.hermandad.dto.CuotaResponseDto;
import com.hermandad.entity.Cuota;
import com.hermandad.mapper.CuotaMapper;
import com.hermandad.service.CuotaService;
import org.springframework.web.bind.annotation.*;

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
}