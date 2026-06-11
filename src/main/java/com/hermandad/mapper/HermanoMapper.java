package com.hermandad.mapper;

import com.hermandad.dto.HermanoRequestDto;
import com.hermandad.dto.HermanoResponseDto;
import com.hermandad.entity.Hermano;
import org.springframework.stereotype.Component;

@Component
public class HermanoMapper {

    public Hermano toEntity(HermanoRequestDto dto) {

        Hermano hermano = new Hermano();

        hermano.setNombre(dto.getNombre());
        hermano.setApellidos(dto.getApellidos());
        hermano.setDni(dto.getDni());
        hermano.setTelefono(dto.getTelefono());
        hermano.setEmail(dto.getEmail());
        hermano.setDireccion(dto.getDireccion());
        hermano.setFechaNacimiento(dto.getFechaNacimiento());
        hermano.setEstado(dto.getEstado());

        return hermano;
    }

    public HermanoResponseDto toResponse(Hermano hermano) {

        HermanoResponseDto dto = new HermanoResponseDto();

        dto.setId(hermano.getId());
        dto.setNumeroHermano(hermano.getNumeroHermano());
        dto.setNombre(hermano.getNombre());
        dto.setApellidos(hermano.getApellidos());
        dto.setDni(hermano.getDni());
        dto.setTelefono(hermano.getTelefono());
        dto.setEmail(hermano.getEmail());
        dto.setDireccion(hermano.getDireccion());
        dto.setFechaNacimiento(hermano.getFechaNacimiento());
        dto.setFechaAlta(hermano.getFechaAlta());
        dto.setEstado(hermano.getEstado());
        dto.setFechaCreacion(hermano.getFechaCreacion());
        dto.setFechaModificacion(hermano.getFechaModificacion());

        return dto;
    }
}