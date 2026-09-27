package com.hermandad.dto;

import com.hermandad.entity.Rol;
import lombok.Data;
import com.hermandad.entity.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UsuarioRequestDto {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;

    @NotNull(message = "El estado activo es obligatorio")
    private Boolean activo;
}