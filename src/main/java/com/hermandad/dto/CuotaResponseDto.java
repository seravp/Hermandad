package com.hermandad.dto;

import com.hermandad.entity.EstadoCuota;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CuotaResponseDto {

    private Long id;

    private Integer anio;

    private BigDecimal importe;

    private EstadoCuota estado;

    private LocalDate fechaPago;

    private String observaciones;

    private Long hermanoId;

    private Integer numeroHermano;
}