package com.hermandad.service;

import com.hermandad.entity.Rol;
import com.hermandad.entity.Usuario;
import com.hermandad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.nio.charset.StandardCharsets;
import org.springframework.transaction.annotation.Transactional;
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

    private Usuario obtenerParaModificar(Long id) {
        return usuarioRepository.findByIdForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private void validarPassword(String password) {
        if (password == null || password.isBlank()
                || password.codePointCount(0, password.length()) < 12
                || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La contraseña debe tener al menos 12 caracteres y como máximo 72 bytes UTF-8");
        }
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

    @Transactional
    public Usuario guardar(
            Usuario usuario) {

        validarPassword(usuario.getPassword());


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


    @Transactional
    public Usuario actualizar(
            Long id,
            Usuario usuarioActualizado) {

        usuarioRepository.lockAdministradores(Rol.ADMIN);
        Usuario usuario = obtenerParaModificar(id);

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
                        && !Boolean.TRUE.equals(
                        usuarioActualizado.getActivo());

        if ((dejaDeSerAdministrador || desactivaAdministrador)
                && Boolean.TRUE.equals(usuario.getActivo())
                && usuarioRepository.countByRolAndActivoTrue(Rol.ADMIN) <= 1) {

            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "No se puede desactivar ni quitar el rol al último administrador");
        }

        String nuevaPassword = usuarioActualizado.getPassword();
        boolean cambiaPassword = nuevaPassword != null && !nuevaPassword.isEmpty();
        if (cambiaPassword) {
            validarPassword(nuevaPassword);
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        }

        usuario.setUsername(
                usuarioActualizado.getUsername());

        usuario.setRol(
                usuarioActualizado.getRol());

        if (cambiaPassword || !java.util.Objects.equals(usuario.getActivo(), usuarioActualizado.getActivo())) {
            usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        }
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

    @Transactional
    public Usuario cambiarPassword(
            Long id,
            String password) {

        Usuario usuario = obtenerParaModificar(id);

        validarPassword(password);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);

        Usuario actualizado =
                usuarioRepository.save(usuario);

        auditoriaService.registrar(
                "CAMBIAR_PASSWORD",
                "USUARIO",
                actualizado.getId());

        return actualizado;
    }

    @Transactional
    public void eliminar(Long id) {
        usuarioRepository.lockAdministradores(Rol.ADMIN);

        Usuario usuario = obtenerParaModificar(id);

        if (usuario.getRol() == Rol.ADMIN
                && Boolean.TRUE.equals(usuario.getActivo())
                && usuarioRepository.countByRolAndActivoTrue(
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
