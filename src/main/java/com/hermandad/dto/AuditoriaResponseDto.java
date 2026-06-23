package com.hermandad.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditoriaResponseDto {

    private Long id;

    private String usuario;

    private String accion;

    private String entidad;

    private Long registroId;

    private LocalDateTime fecha;
}