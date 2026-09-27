package com.hermandad.repository;

import com.hermandad.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditoriaRepository
        extends JpaRepository<Auditoria, Long> {

    Page<Auditoria> findByUsuarioContainingIgnoreCaseAndAccionContainingIgnoreCaseAndEntidadContainingIgnoreCase(
            String usuario,
            String accion,
            String entidad,
            Pageable pageable);
}