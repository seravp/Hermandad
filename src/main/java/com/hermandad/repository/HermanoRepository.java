package com.hermandad.repository;

import com.hermandad.entity.EstadoHermano;
import com.hermandad.entity.Hermano;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface HermanoRepository extends JpaRepository<Hermano, Long> {
    boolean existsByDni(String dni);
    List<Hermano> findByApellidosContainingIgnoreCase(String apellidos);

    Optional<Hermano> findByDni(String dni);

    List<Hermano> findByEstado(EstadoHermano estado);

    @Query("SELECT COALESCE(MAX(h.numeroHermano), 0) FROM Hermano h")
    Integer obtenerUltimoNumeroHermano();

}
