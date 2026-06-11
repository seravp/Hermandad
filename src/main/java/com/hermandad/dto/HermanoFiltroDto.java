package com.hermandad.dto;

import com.hermandad.entity.EstadoHermano;
import lombok.Data;

@Data
public class HermanoFiltroDto {

    private String apellidos;

    private EstadoHermano estado;


}
