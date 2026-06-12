package com.hermandad.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Entity
@Table(name = "hermanos")
public class Hermano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 9, max = 9, message = "El DNI debe tener 9 caracteres")
    private String dni;

    @Email(message = "Email no válido")
    private String email;

    @Column(unique = true)
    private Integer numeroHermano;

    private String telefono;

    private String direccion;

    private LocalDate fechaNacimiento;

    private LocalDate fechaAlta;

    @Enumerated(EnumType.STRING)
    private EstadoHermano estado;

    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaModificacion;

    @JsonIgnore
    @OneToMany(mappedBy = "hermano")
    private List<Cuota> cuotas;

    @Size(max = 34)
    private String iban;

    private String titularCuenta;

    @Enumerated(EnumType.STRING)
    private FormaPago formaPago;

    public Hermano() {
    }
}