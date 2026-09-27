package com.hermandad.dto;

import com.hermandad.entity.EstadoInventario;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class InventarioResponseDto {
    private Long id;
    private String codigo;
    private String nombre;
    private String categoria;
    private String descripcion;
    private String ubicacion;
    private EstadoInventario estado;
    private LocalDate fechaAdquisicion;
    private BigDecimal valorAdquisicion;
    private Boolean activo;
    private String observaciones;
}
