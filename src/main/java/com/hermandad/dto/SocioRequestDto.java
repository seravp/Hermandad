package com.hermandad.dto;

import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.FormaPago;
import com.hermandad.entity.TipoSocio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

import java.time.LocalDate;


@Data
public class SocioRequestDto {


    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;
    @NotBlank(message = "El DNI es obligatorio")
    private String dni;

    private String telefono;

    @Email(message = "Email no válido")
    private String email;

    private String direccion;

    private LocalDate fechaNacimiento;

    private EstadoSocio estado;

    private TipoSocio tipo;

    private String iban;

    private String titularCuenta;

    private FormaPago formaPago;
}
