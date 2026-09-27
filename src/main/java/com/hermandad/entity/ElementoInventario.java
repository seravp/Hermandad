package com.hermandad.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "inventario", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class ElementoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String categoria;

    @Column(length = 2000)
    private String descripcion;

    @Column(nullable = false)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoInventario estado;

    private LocalDate fechaAdquisicion;

    @Column(precision = 12, scale = 2)
    private BigDecimal valorAdquisicion;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(length = 2000)
    private String observaciones;
}
