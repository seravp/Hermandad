package com.hermandad.mapper;

import com.hermandad.dto.ConfiguracionDto;
import com.hermandad.entity.Configuracion;
import org.springframework.stereotype.Component;

@Component
public class ConfiguracionMapper {

    public Configuracion toEntity(ConfiguracionDto dto) {

        Configuracion configuracion = new Configuracion();

        configuracion.setNombreHermandad(dto.getNombreHermandad());
        configuracion.setCif(dto.getCif());
        configuracion.setDireccion(dto.getDireccion());
        configuracion.setTelefono(dto.getTelefono());
        configuracion.setEmail(dto.getEmail());
        configuracion.setImporteCuotaHermano(dto.getImporteCuotaHermano());
        configuracion.setImporteCuotaCostalero(dto.getImporteCuotaCostalero());
        configuracion.setAnioActivo(dto.getAnioActivo());
        configuracion.setIban(dto.getIban());

        return configuracion;
    }

    public ConfiguracionDto toResponse(Configuracion configuracion) {

        ConfiguracionDto dto = new ConfiguracionDto();

        dto.setNombreHermandad(configuracion.getNombreHermandad());
        dto.setCif(configuracion.getCif());
        dto.setDireccion(configuracion.getDireccion());
        dto.setTelefono(configuracion.getTelefono());
        dto.setEmail(configuracion.getEmail());
        dto.setImporteCuotaHermano(configuracion.getImporteCuotaHermano());
        dto.setImporteCuotaCostalero(configuracion.getImporteCuotaCostalero());
        dto.setAnioActivo(configuracion.getAnioActivo());
        dto.setIban(configuracion.getIban());

        return dto;
    }

}
