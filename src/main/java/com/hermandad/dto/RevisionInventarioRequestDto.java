package com.hermandad.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RevisionInventarioRequestDto {

    @NotBlank(message = "El título de la revisión es obligatorio.")
    private String titulo;

    private String observaciones;
}
