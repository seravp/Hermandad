package com.hermandad.repository;

import com.hermandad.entity.RevisionInventarioDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RevisionInventarioDetalleRepository extends JpaRepository<RevisionInventarioDetalle, Long> {

    List<RevisionInventarioDetalle> findByRevisionIdOrderByElementoCodigoAsc(Long revisionId);

    long countByRevisionId(Long revisionId);

    long countByRevisionIdAndVerificadoTrue(Long revisionId);

    long countByRevisionIdAndIncidenciaIsNotNull(Long revisionId);
}
