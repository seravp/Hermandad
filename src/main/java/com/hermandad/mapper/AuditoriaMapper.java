package com.hermandad.mapper;

import com.hermandad.dto.AuditoriaResponseDto;
import com.hermandad.entity.Auditoria;
import org.springframework.stereotype.Component;

@Component
public class AuditoriaMapper {

    public AuditoriaResponseDto toResponse(
            Auditoria auditoria) {

        AuditoriaResponseDto dto =
                new AuditoriaResponseDto();

        dto.setId(auditoria.getId());
        dto.setUsuario(auditoria.getUsuario());
        dto.setAccion(auditoria.getAccion());
        dto.setEntidad(auditoria.getEntidad());
        dto.setRegistroId(auditoria.getRegistroId());
        dto.setFecha(auditoria.getFecha());

        return dto;
    }
}
