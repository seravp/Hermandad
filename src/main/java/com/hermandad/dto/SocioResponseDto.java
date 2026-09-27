package com.hermandad.dto;

import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.FormaPago;
import com.hermandad.entity.TipoSocio;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "id",
        "numeroSocio",
        "nombre",
        "apellidos",
        "dni",
        "telefono",
        "email",
        "direccion",
        "fechaNacimiento",
        "fechaAlta",
        "estado"
})
@Data
public class SocioResponseDto {

    private Long id;

    private Integer numeroSocio;

    private String nombre;

    private String apellidos;

    private String dni;

    private String telefono;

    private String email;

    private String direccion;

    private LocalDate fechaNacimiento;

    private LocalDate fechaAlta;

    private EstadoSocio estado;

    private TipoSocio tipo;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    private String iban;

    private String titularCuenta;

    private FormaPago formaPago;
}


