package com.hermandad.mapper;

import com.hermandad.dto.UsuarioRequestDto;
import com.hermandad.dto.UsuarioResponseDto;
import com.hermandad.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public Usuario toEntity(
            UsuarioRequestDto dto) {

        Usuario usuario = new Usuario();

        usuario.setUsername(
                dto.getUsername());

        usuario.setPassword(
                dto.getPassword());

        usuario.setRol(
                dto.getRol());

        usuario.setActivo(
                dto.getActivo());

        return usuario;
    }

    public UsuarioResponseDto toResponse(
            Usuario usuario) {

        UsuarioResponseDto dto =
                new UsuarioResponseDto();

        dto.setId(
                usuario.getId());

        dto.setUsername(
                usuario.getUsername());

        dto.setRol(
                usuario.getRol());

        dto.setActivo(
                usuario.getActivo());

        return dto;
    }
}