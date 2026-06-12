package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartaMorosoDto {

    private String nombreCompleto;

    private Long cuotasPendientes;

    private BigDecimal importePendiente;
}