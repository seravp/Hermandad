package com.hermandad.repository;

import com.hermandad.entity.ConfiguracionCuadrilla;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConfiguracionCuadrillaRepository extends JpaRepository<ConfiguracionCuadrilla, Long> {
    Optional<ConfiguracionCuadrilla> findByNombre(String nombre);
    List<ConfiguracionCuadrilla> findAllByOrderByNombreAsc();
}
