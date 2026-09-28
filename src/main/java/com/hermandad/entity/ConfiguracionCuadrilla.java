package com.hermandad.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "configuracion_cuadrillas", uniqueConstraints = @UniqueConstraint(columnNames = "nombre"))
public class ConfiguracionCuadrilla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private Integer filas;

    @Column(nullable = false)
    private Integer columnas;
}
