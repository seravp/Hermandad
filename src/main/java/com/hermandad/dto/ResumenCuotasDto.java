package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ResumenCuotasDto {

    private Long totalCuotas;

    private Long pendientes;

    private Long pagadas;

    private Long anuladas;

    private BigDecimal importePendiente;

    private BigDecimal importeCobrado;
}