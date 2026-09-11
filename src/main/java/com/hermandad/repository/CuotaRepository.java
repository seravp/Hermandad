package com.hermandad.repository;

import com.hermandad.entity.Cuota;
import com.hermandad.entity.EstadoCuota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.util.List;

public interface CuotaRepository
        extends JpaRepository<Cuota, Long> {

    List<Cuota> findByHermanoId(Long hermanoId);

    List<Cuota> findByEstado(EstadoCuota estado);

    boolean existsByHermanoIdAndAnio(Long hermanoId, Integer anio);

    long countByEstado(EstadoCuota estado);

    @Query("""
       SELECT COALESCE(SUM(c.importe), 0)
       FROM Cuota c
       WHERE c.estado = 'PAGADA'
       """)
    java.math.BigDecimal totalRecaudado();

    @Query("""
       SELECT COALESCE(SUM(c.importe), 0)
       FROM Cuota c
       WHERE c.estado = 'PENDIENTE'
       """)
    java.math.BigDecimal totalPendiente();

    @Query("""
    SELECT c
    FROM Cuota c
    JOIN c.hermano h
    WHERE
    (
        LOWER(h.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR LOWER(h.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))
        OR CAST(h.numeroHermano AS string) LIKE CONCAT('%', :texto, '%')
    )
    AND (:estado IS NULL OR c.estado = :estado)
    AND (:anio IS NULL OR c.anio = :anio)
    """)
    Page<Cuota> buscar(
            @Param("texto") String texto,
            @Param("estado") EstadoCuota estado,
            @Param("anio") Integer anio,
            Pageable pageable);

    @Query("""
    SELECT DISTINCT c.anio
    FROM Cuota c
    ORDER BY c.anio DESC
""")
    List<Integer> obtenerAniosDisponibles();

    long countByEstadoAndAnio(
            EstadoCuota estado,
            Integer anio);

    @Query("""
        SELECT COALESCE(SUM(c.importe),0)
        FROM Cuota c
        WHERE c.estado = :estado
        AND c.anio = :anio
        """)
    BigDecimal sumImporteByEstadoAndAnio(
            EstadoCuota estado,
            Integer anio);

    @Query("""
        SELECT COUNT(DISTINCT c.hermano.id)
        FROM Cuota c
        WHERE c.estado = 'PENDIENTE'
        AND c.anio = :anio
        """)
    Long contarMorosos(Integer anio);

}