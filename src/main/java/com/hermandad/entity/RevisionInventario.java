package com.hermandad.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "revisiones_inventario")
public class RevisionInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private String usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRevisionInventario estado;

    @Column(length = 2000)
    private String observaciones;
}
