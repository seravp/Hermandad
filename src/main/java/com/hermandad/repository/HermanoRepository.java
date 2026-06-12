package com.hermandad.repository;

import com.hermandad.entity.EstadoHermano;
import com.hermandad.entity.FormaPago;
import com.hermandad.entity.Hermano;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface HermanoRepository extends JpaRepository<Hermano, Long> {
    boolean existsByDni(String dni);
    List<Hermano> findByApellidosContainingIgnoreCase(String apellidos);

    Optional<Hermano> findByDni(String dni);

    List<Hermano> findByEstado(EstadoHermano estado);

    List<Hermano> findByFormaPago(FormaPago formaPago);

    @Query("SELECT COALESCE(MAX(h.numeroHermano), 0) FROM Hermano h")
    Integer obtenerUltimoNumeroHermano();

    @Query("""
    SELECT h
    FROM Hermano h
    WHERE
        LOWER(h.apellidos)
        LIKE LOWER(CONCAT('%', :apellidos, '%'))
    AND
        LOWER(h.nombre)
        LIKE LOWER(CONCAT('%', :nombre, '%'))
    AND
        LOWER(h.dni)
        LIKE LOWER(CONCAT('%', :dni, '%'))
    AND
        (:estado IS NULL
            OR h.estado = :estado)
""")
    Page<Hermano> buscar(
            @Param("nombre") String nombre,
            @Param("apellidos") String apellidos,
            @Param("dni") String dni,
            @Param("estado") EstadoHermano estado,
            Pageable pageable);

}
