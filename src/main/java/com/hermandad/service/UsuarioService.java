package com.hermandad.service;

import com.hermandad.entity.Rol;
import com.hermandad.entity.Usuario;
import com.hermandad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

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
                .orElseThrow();
    }

    public Usuario guardar(
            Usuario usuario) {

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

        Usuario usuario =
                obtenerPorId(id);

        if (usuario.getRol() == Rol.ADMIN
                && usuarioActualizado.getRol() != Rol.ADMIN
                && usuarioRepository.countByRol(
                Rol.ADMIN) <= 1) {

            throw new RuntimeException(
                    "No se puede quitar el rol al último administrador");
        }

        usuario.setRol(
                usuarioActualizado.getRol());

        if (usuario.getRol() == Rol.ADMIN
                && Boolean.FALSE.equals(
                usuarioActualizado.getActivo())
                && usuarioRepository.countByRol(
                Rol.ADMIN) <= 1) {

            throw new RuntimeException(
                    "No se puede desactivar el último administrador");
        }

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

            throw new RuntimeException(
                    "No se puede eliminar el último administrador");
        }

        auditoriaService.registrar(
                "ELIMINAR",
                "USUARIO",
                id);

        usuarioRepository.deleteById(id);
    }
}
