package com.hermandad.mapper;

import com.hermandad.dto.CuotaRequestDto;
import com.hermandad.dto.CuotaResponseDto;
import com.hermandad.entity.Cuota;
import org.springframework.stereotype.Component;

@Component
public class CuotaMapper {

    public CuotaResponseDto toResponse(Cuota cuota) {

        CuotaResponseDto dto =
                new CuotaResponseDto();

        dto.setId(cuota.getId());
        dto.setAnio(cuota.getAnio());
        dto.setImporte(cuota.getImporte());
        dto.setEstado(cuota.getEstado());
        dto.setFechaPago(cuota.getFechaPago());
        dto.setObservaciones(cuota.getObservaciones());

        dto.setHermanoId(
                cuota.getHermano().getId());

        dto.setNumeroHermano(
                cuota.getHermano()
                        .getNumeroHermano());

        return dto;
    }

    public Cuota toEntity(
            CuotaRequestDto dto) {

        Cuota cuota = new Cuota();

        cuota.setAnio(dto.getAnio());
        cuota.setImporte(dto.getImporte());
        cuota.setObservaciones(dto.getObservaciones());

        return cuota;
    }
}
