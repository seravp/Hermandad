package com.hermandad.repository;

import com.hermandad.entity.Cuota;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CuotaRepository
        extends JpaRepository<Cuota, Long> {

}