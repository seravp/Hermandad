package com.hermandad.repository;

import com.hermandad.entity.Configuracion;
import com.hermandad.entity.EstadoCuota;
import com.hermandad.entity.EstadoHermano;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ConfiguracionRepository
        extends JpaRepository<Configuracion, Long> {

}
