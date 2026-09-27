package com.hermandad.dto;

import com.hermandad.entity.EstadoInventario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevisionInventarioDetalleResponseDto {
    private Long id;
    private Long elementoId;
    private String codigo;
    private String nombre;
    private String categoria;
    private String ubicacionRegistrada;
    private EstadoInventario estadoRegistrado;
    private Boolean verificado;
    private EstadoInventario estadoObservado;
    private String ubicacionObservada;
    private String incidencia;
    private LocalDateTime fechaVerificacion;
}
