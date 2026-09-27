package com.hermandad.repository;

import com.hermandad.entity.ElementoInventario;
import com.hermandad.entity.EstadoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ElementoInventarioRepository extends JpaRepository<ElementoInventario, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    List<ElementoInventario> findByActivoTrue();

    @Query("""
            SELECT e FROM ElementoInventario e
            WHERE (:texto = '' OR LOWER(e.codigo) LIKE LOWER(CONCAT('%', :texto, '%'))
                OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:categoria = '' OR LOWER(e.categoria) = LOWER(:categoria))
              AND (:estado IS NULL OR e.estado = :estado)
            """)
    Page<ElementoInventario> buscar(
            @Param("texto") String texto,
            @Param("categoria") String categoria,
            @Param("estado") EstadoInventario estado,
            Pageable pageable);
}
