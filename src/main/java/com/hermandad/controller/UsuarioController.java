package com.hermandad.controller;

import com.hermandad.dto.CambiarPasswordDto;
import com.hermandad.dto.UsuarioRequestDto;
import com.hermandad.dto.UsuarioResponseDto;
import com.hermandad.entity.Usuario;
import com.hermandad.mapper.UsuarioMapper;
import com.hermandad.service.UsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper) {

        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }

    @GetMapping
    public List<UsuarioResponseDto> obtenerTodos() {

        return usuarioService
                .obtenerTodos()
                .stream()
                .map(usuarioMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public UsuarioResponseDto obtenerPorId(
            @PathVariable Long id) {

        Usuario usuario =
                usuarioService.obtenerPorId(id);

        return usuarioMapper.toResponse(usuario);
    }

    @PostMapping
    public UsuarioResponseDto crear(
            @RequestBody UsuarioRequestDto dto) {

        Usuario usuario =
                usuarioMapper.toEntity(dto);

        Usuario guardado =
                usuarioService.guardar(usuario);

        return usuarioMapper.toResponse(guardado);
    }

    @DeleteMapping("/{id}")
    public void eliminar(
            @PathVariable Long id) {

        usuarioService.eliminar(id);
    }

    @PutMapping("/{id}")
    public UsuarioResponseDto actualizar(
            @PathVariable Long id,
            @RequestBody UsuarioRequestDto dto) {

        Usuario usuario =
                usuarioMapper.toEntity(dto);

        Usuario actualizado =
                usuarioService.actualizar(
                        id,
                        usuario);

        return usuarioMapper.toResponse(
                actualizado);
    }

    @PutMapping("/{id}/password")
    public UsuarioResponseDto cambiarPassword(
            @PathVariable Long id,
            @RequestBody CambiarPasswordDto dto) {

        Usuario usuario =
                usuarioService.cambiarPassword(
                        id,
                        dto.getPassword());

        return usuarioMapper.toResponse(
                usuario);
    }
}
