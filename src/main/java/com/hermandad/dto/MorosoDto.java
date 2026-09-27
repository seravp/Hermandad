package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MorosoDto {

    private Long socioId;

    private Integer numeroSocio;

    private String nombreCompleto;

    private Long cuotasPendientes;

    private BigDecimal importePendiente;
}