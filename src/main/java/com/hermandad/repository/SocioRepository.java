package com.hermandad.repository;

import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.FormaPago;
import com.hermandad.entity.Socio;
import com.hermandad.entity.TipoSocio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SocioRepository extends JpaRepository<Socio, Long> {
    boolean existsByDni(String dni);
    List<Socio> findByApellidosContainingIgnoreCase(String apellidos);

    Optional<Socio> findByDni(String dni);

    List<Socio> findByEstado(EstadoSocio estado);

    List<Socio> findByFormaPago(FormaPago formaPago);

    @Query("SELECT COALESCE(MAX(h.numeroSocio), 0) FROM Socio h")
    Integer obtenerUltimoNumeroSocio();

    @Query("""
    SELECT h
    FROM Socio h
    WHERE
        (
            :texto = ''
            OR LOWER(h.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(h.apellidos) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR LOWER(h.dni) LIKE LOWER(CONCAT('%', :texto, '%'))
            OR CAST(h.numeroSocio AS string) LIKE CONCAT('%', :texto, '%')
        )
    AND
        (:estado IS NULL OR h.estado = :estado)
    AND
        (:tipo IS NULL OR h.tipo = :tipo)
""")
    Page<Socio> buscar(
            @Param("texto") String texto,
            @Param("estado") EstadoSocio estado,
            @Param("tipo") TipoSocio tipo,
            Pageable pageable);

    long countByEstado(EstadoSocio estado);

    long countByEstadoAndTipo(EstadoSocio estado, TipoSocio tipo);

}
