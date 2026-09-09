package com.hermandad.repository;

import com.hermandad.entity.Rol;
import com.hermandad.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    long countByRol(
            Rol rol);

    Optional<Usuario> findByUsername(
            String username);
}
