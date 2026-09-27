package com.hermandad.dto;

import com.hermandad.entity.EstadoInventario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventarioRequestDto {

    @NotBlank(message = "El código es obligatorio.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    @NotBlank(message = "La categoría es obligatoria.")
    private String categoria;

    private String descripcion;

    @NotBlank(message = "La ubicación es obligatoria.")
    private String ubicacion;

    @NotNull(message = "El estado es obligatorio.")
    private EstadoInventario estado;

    private LocalDate fechaAdquisicion;

    @PositiveOrZero(message = "El valor no puede ser negativo.")
    private BigDecimal valorAdquisicion;

    private String observaciones;
}
