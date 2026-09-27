package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardDto {

    private Long totalSocios;

    private Long totalHermanos;

    private Long totalCostaleros;

    private Long cuotasPagadas;

    private Long cuotasPendientes;

    private Long morosos;

    private BigDecimal importeRecaudado;

    private BigDecimal importePendiente;

    private Integer anioActivo;

    private Integer porcentajeCobrado;

    private Long totalCuotas;


}
