package com.hermandad.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "cuotas")
public class Cuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer anio;

    private BigDecimal importe;

    @Enumerated(EnumType.STRING)
    private EstadoCuota estado;

    private LocalDate fechaPago;

    private String observaciones;

    @ManyToOne
    @JoinColumn(name = "hermano_id")
    private Hermano hermano;
}
