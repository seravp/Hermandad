package com.hermandad.repository;

import com.hermandad.entity.RevisionInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevisionInventarioRepository extends JpaRepository<RevisionInventario, Long> {

    List<RevisionInventario> findAllByOrderByFechaDesc();
}
