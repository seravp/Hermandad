package com.hermandad.dto;

import com.hermandad.entity.EstadoSocio;
import lombok.Data;

@Data
public class SocioFiltroDto {

    private String apellidos;

    private EstadoSocio estado;


}
