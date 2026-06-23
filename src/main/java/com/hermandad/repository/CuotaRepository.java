package com.hermandad.repository;

import com.hermandad.entity.Cuota;
import com.hermandad.entity.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


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

}