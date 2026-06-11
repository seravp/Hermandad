package com.hermandad.dto;

import com.hermandad.entity.EstadoHermano;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({
        "id",
        "numeroHermano",
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
public class HermanoResponseDto {

    private Long id;

    private Integer numeroHermano;

    private String nombre;

    private String apellidos;

    private String dni;

    private String telefono;

    private String email;

    private String direccion;

    private LocalDate fechaNacimiento;

    private LocalDate fechaAlta;

    private EstadoHermano estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;
}


