package com.hermandad.dto;

import com.hermandad.entity.Rol;
import lombok.Data;

@Data
public class UsuarioRequestDto {

    private String username;

    private String password;

    private Rol rol;

    private Boolean activo;
}