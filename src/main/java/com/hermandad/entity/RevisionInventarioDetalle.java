package com.hermandad.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "revisiones_inventario_detalle",
        uniqueConstraints = @UniqueConstraint(columnNames = {"revision_id", "elemento_id"}))
public class RevisionInventarioDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "revision_id", nullable = false)
    private RevisionInventario revision;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "elemento_id", nullable = false)
    private ElementoInventario elemento;

    @Column(nullable = false)
    private Boolean verificado = false;

    @Enumerated(EnumType.STRING)
    private EstadoInventario estadoObservado;

    private String ubicacionObservada;

    @Column(length = 2000)
    private String incidencia;

    private LocalDateTime fechaVerificacion;
}
