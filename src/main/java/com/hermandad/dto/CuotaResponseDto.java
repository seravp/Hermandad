package com.hermandad.dto;

import com.hermandad.entity.EstadoCuota;
import com.hermandad.entity.TipoSocio;
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

    private Long socioId;

    private Integer numeroSocio;

    private String nombreSocio;

    private TipoSocio tipo;

    private String cuadrilla;
}
