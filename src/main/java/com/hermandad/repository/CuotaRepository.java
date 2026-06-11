package com.hermandad.repository;

import com.hermandad.entity.Cuota;
import com.hermandad.entity.EstadoCuota;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface CuotaRepository
        extends JpaRepository<Cuota, Long> {

    List<Cuota> findByHermanoId(Long hermanoId);

    List<Cuota> findByEstado(EstadoCuota estado);

    boolean existsByHermanoIdAndAnio(Long hermanoId, Integer anio);

    long countByEstado(EstadoCuota estado);

}