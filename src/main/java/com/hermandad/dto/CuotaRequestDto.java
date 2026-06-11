package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CuotaRequestDto {

    private Long hermanoId;

    private Integer anio;

    private BigDecimal importe;

    private String observaciones;
}