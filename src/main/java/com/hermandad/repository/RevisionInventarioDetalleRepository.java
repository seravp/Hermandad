package com.hermandad.repository;

import com.hermandad.entity.RevisionInventarioDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;

public interface RevisionInventarioDetalleRepository extends JpaRepository<RevisionInventarioDetalle, Long> {

    @EntityGraph(attributePaths = "elemento")
    List<RevisionInventarioDetalle> findByRevisionIdOrderByElementoCodigoAsc(Long revisionId);

    @Override
    @EntityGraph(attributePaths = {"elemento", "revision"})
    Optional<RevisionInventarioDetalle> findById(Long id);

    long countByRevisionId(Long revisionId);

    long countByRevisionIdAndVerificadoTrue(Long revisionId);

    long countByRevisionIdAndIncidenciaIsNotNull(Long revisionId);
}
