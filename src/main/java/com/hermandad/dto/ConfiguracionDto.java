package com.hermandad.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ConfiguracionDto {

    private String nombreHermandad;

    private String cif;

    private String direccion;

    private String telefono;

    private String email;

    private BigDecimal importeCuota;

    private Integer anioActivo;

    private String iban;

}
