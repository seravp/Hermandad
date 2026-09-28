package com.hermandad.dto;

import java.math.BigDecimal;

public record ConfiguracionCuotasDto(
        Integer anioActivo,
        BigDecimal importeCuotaHermano,
        BigDecimal importeCuotaCostalero) {
}
