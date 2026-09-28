package com.hermandad.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ConfiguracionDto {

    private String nombreHermandad;

    private String cif;

    private String direccion;

    private String telefono;

    private String email;

    private BigDecimal importeCuotaHermano;

    private BigDecimal importeCuotaCostalero;

    private Integer anioActivo;

    private String iban;

    private List<ConfiguracionCuadrillaDto> cuadrillas;

}
