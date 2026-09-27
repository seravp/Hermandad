package com.hermandad.dto;

import com.hermandad.entity.EstadoInventario;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RevisionInventarioDetalleRequestDto {

    @NotNull(message = "Debes indicar si el bien ha sido verificado.")
    private Boolean verificado;

    private EstadoInventario estadoObservado;
    private String ubicacionObservada;
    private String incidencia;
}
