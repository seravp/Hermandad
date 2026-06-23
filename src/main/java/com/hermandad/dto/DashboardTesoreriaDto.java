package com.hermandad.dto;

import com.hermandad.entity.EstadoCuota;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DashboardTesoreriaDto {

    private Long totalHermanos;

    private Long cuotasPagadas;

    private Long cuotasPendientes;

    private Long morosos;

    private BigDecimal importeRecaudado;

    private BigDecimal importePendiente;


}