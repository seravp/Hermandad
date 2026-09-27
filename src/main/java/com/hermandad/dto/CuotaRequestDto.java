package com.hermandad.dto;

import lombok.Data;

import java.math.BigDecimal;
import com.hermandad.entity.TipoSocio;

@Data
public class CuotaRequestDto {

    private Long socioId;

    private Integer anio;

    private BigDecimal importe;

    private String observaciones;

    private TipoSocio tipo;
}
