package com.hermandad.service;

import com.hermandad.entity.Usuario;
import com.hermandad.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository
            usuarioRepository;

    public UsuarioService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository =
                usuarioRepository;
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

        return usuarioRepository.save(usuario);
    }

    public void eliminar(
            Long id) {

        usuarioRepository.deleteById(id);
    }

    public Usuario actualizar(
            Long id,
            Usuario usuarioActualizado) {

        Usuario usuario =
                obtenerPorId(id);

        usuario.setRol(
                usuarioActualizado.getRol());

        usuario.setActivo(
                usuarioActualizado.getActivo());

        return usuarioRepository.save(
                usuario);
    }

    public Usuario cambiarPassword(
            Long id,
            String password) {

        Usuario usuario =
                obtenerPorId(id);

        usuario.setPassword(password);

        return usuarioRepository.save(
                usuario);
    }
}
