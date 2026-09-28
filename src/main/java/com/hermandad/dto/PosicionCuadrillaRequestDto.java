package com.hermandad.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PosicionCuadrillaRequestDto(
        @NotNull(message = "La posición es obligatoria.")
        @Min(value = 1, message = "La posición mínima es 1.")
        @Max(value = 160, message = "La posición máxima es 160.")
        Integer posicion) {
}
