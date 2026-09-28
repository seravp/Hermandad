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
        dto.setTipo(cuota.getTipo());
        dto.setCuadrilla(cuota.getSocio().getCuadrilla());

        dto.setSocioId(
                cuota.getSocio().getId());

        dto.setNumeroSocio(
                cuota.getSocio()
                        .getNumeroSocio());

        dto.setNombreSocio(
                cuota.getSocio().getNombre() + " " +
                        cuota.getSocio().getApellidos());

        return dto;
    }

    public Cuota toEntity(
            CuotaRequestDto dto) {

        Cuota cuota = new Cuota();

        cuota.setAnio(dto.getAnio());
        cuota.setImporte(dto.getImporte());
        cuota.setObservaciones(dto.getObservaciones());
        cuota.setTipo(dto.getTipo());

        return cuota;
    }
}
