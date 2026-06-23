package com.hermandad.dto;

import com.hermandad.entity.Rol;
import lombok.Data;

@Data
public class UsuarioResponseDto {

    private Long id;

    private String username;

    private Rol rol;

    private Boolean activo;
}