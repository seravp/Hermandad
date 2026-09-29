package com.hermandad.repository;

import com.hermandad.entity.Rol;
import com.hermandad.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {
    boolean existsByUsername(String username);

    long countByRolAndActivoTrue(Rol rol);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.rol = :rol order by u.id")
    java.util.List<Usuario> lockAdministradores(@org.springframework.data.repository.query.Param("rol") Rol rol);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);

    long countByRol(
            Rol rol);

    Optional<Usuario> findByUsername(
            String username);
}
