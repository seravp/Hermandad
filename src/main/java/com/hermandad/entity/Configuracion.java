package com.hermandad.entity;

import jakarta.persistence.*;
import lombok.Data;


import java.math.BigDecimal;

@Data
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    private Long id;

    private String nombreHermandad;

    private String cif;

    private String direccion;

    private String telefono;

    private String email;

    private BigDecimal importeCuota;

    private Integer anioActivo;

    private String iban;

    // getters/setters
}