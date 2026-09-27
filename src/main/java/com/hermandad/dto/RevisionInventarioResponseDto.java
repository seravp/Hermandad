package com.hermandad.dto;

import com.hermandad.entity.EstadoRevisionInventario;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevisionInventarioResponseDto {
    private Long id;
    private String titulo;
    private LocalDateTime fecha;
    private String usuario;
    private EstadoRevisionInventario estado;
    private String observaciones;
    private long totalElementos;
    private long verificados;
    private long incidencias;
}
