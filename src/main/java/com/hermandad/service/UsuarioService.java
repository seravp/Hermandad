package com.hermandad.service;

import com.hermandad.entity.Rol;
import com.hermandad.entity.Usuario;
import com.hermandad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private final UsuarioRepository
            usuarioRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuditoriaService auditoriaService;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService) {

        this.usuarioRepository =
                usuarioRepository;

        this.passwordEncoder =
                passwordEncoder;

        this.auditoriaService =
                auditoriaService;
    }

    public List<Usuario> obtenerTodos() {

        return usuarioRepository.findAll();
    }

    public Usuario obtenerPorId(
            Long id) {

        return usuarioRepository
                .findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado"));
    }

    public Usuario guardar(
            Usuario usuario) {

        if (usuario.getPassword() == null
                || usuario.getPassword().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La contraseña es obligatoria al crear un usuario");
        }

        if (usuarioRepository.existsByUsername(
                usuario.getUsername())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese nombre");
        }

        usuario.setPassword(
                passwordEncoder.encode(
                        usuario.getPassword()));

        Usuario guardado =
                usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "CREAR",
                "USUARIO",
                guardado.getId());

        return guardado;
    }


    public Usuario actualizar(
            Long id,
            Usuario usuarioActualizado) {

        Usuario usuario = obtenerPorId(id);

        if (!usuario.getUsername().equals(
                usuarioActualizado.getUsername())
                && usuarioRepository.existsByUsername(
                usuarioActualizado.getUsername())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese nombre");
        }

        boolean dejaDeSerAdministrador =
                usuario.getRol() == Rol.ADMIN
                        && usuarioActualizado.getRol() != Rol.ADMIN;

        boolean desactivaAdministrador =
                usuario.getRol() == Rol.ADMIN
                        && Boolean.FALSE.equals(
                        usuarioActualizado.getActivo());

        if ((dejaDeSerAdministrador || desactivaAdministrador)
                && usuarioRepository.countByRol(Rol.ADMIN) <= 1) {

            throw new RuntimeException(
                    "No se puede desactivar ni quitar el rol al último administrador");
        }

        usuario.setUsername(
                usuarioActualizado.getUsername());

        usuario.setRol(
                usuarioActualizado.getRol());

        usuario.setActivo(
                usuarioActualizado.getActivo());

        Usuario actualizado =
                usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "MODIFICAR",
                "USUARIO",
                actualizado.getId());

        return actualizado;
    }

    public Usuario cambiarPassword(
            Long id,
            String password) {

        Usuario usuario =
                obtenerPorId(id);

        usuario.setPassword(
                passwordEncoder.encode(
                        password));

        Usuario actualizado =
                usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "CAMBIAR_PASSWORD",
                "USUARIO",
                actualizado.getId());

        return actualizado;
    }

    public void eliminar(Long id) {

        Usuario usuario =
                obtenerPorId(id);

        if (usuario.getRol() == Rol.ADMIN
                && usuarioRepository.countByRol(
                Rol.ADMIN) <= 1) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede desactivar ni quitar el rol al último administrador");
        }

        auditoriaService.registrar(
                "ELIMINAR",
                "USUARIO",
                id);

        usuarioRepository.deleteById(id);
    }
}
