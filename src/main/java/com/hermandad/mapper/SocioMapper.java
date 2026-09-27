package com.hermandad.mapper;

import com.hermandad.dto.SocioRequestDto;
import com.hermandad.dto.SocioResponseDto;
import com.hermandad.entity.Socio;
import org.springframework.stereotype.Component;

@Component
public class SocioMapper {

    public Socio toEntity(SocioRequestDto dto) {

        Socio socio = new Socio();

        socio.setNombre(dto.getNombre());
        socio.setApellidos(dto.getApellidos());
        socio.setDni(dto.getDni());
        socio.setTelefono(dto.getTelefono());
        socio.setEmail(dto.getEmail());
        socio.setDireccion(dto.getDireccion());
        socio.setFechaNacimiento(dto.getFechaNacimiento());
        socio.setEstado(dto.getEstado());
        socio.setTipo(dto.getTipo());
        socio.setIban(dto.getIban());
        socio.setTitularCuenta(dto.getTitularCuenta());
        socio.setFormaPago(dto.getFormaPago());

        return socio;
    }

    public SocioResponseDto toResponse(Socio socio) {

        SocioResponseDto dto = new SocioResponseDto();

        dto.setId(socio.getId());
        dto.setNumeroSocio(socio.getNumeroSocio());
        dto.setNombre(socio.getNombre());
        dto.setApellidos(socio.getApellidos());
        dto.setDni(socio.getDni());
        dto.setTelefono(socio.getTelefono());
        dto.setEmail(socio.getEmail());
        dto.setDireccion(socio.getDireccion());
        dto.setFechaNacimiento(socio.getFechaNacimiento());
        dto.setFechaAlta(socio.getFechaAlta());
        dto.setEstado(socio.getEstado());
        dto.setTipo(socio.getTipo());
        dto.setFechaCreacion(socio.getFechaCreacion());
        dto.setFechaModificacion(socio.getFechaModificacion());
        dto.setIban(socio.getIban());
        dto.setTitularCuenta(socio.getTitularCuenta());
        dto.setFormaPago(socio.getFormaPago());

        return dto;
    }
}
