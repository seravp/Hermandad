package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MorosoDto {

    private Long hermanoId;

    private Integer numeroHermano;

    private String nombreCompleto;

    private Long cuotasPendientes;

    private BigDecimal importePendiente;
}