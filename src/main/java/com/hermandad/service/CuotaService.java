package com.hermandad.service;

import com.hermandad.entity.Cuota;
import com.hermandad.entity.EstadoCuota;
import com.hermandad.entity.Hermano;
import com.hermandad.exception.RecursoNoEncontradoException;
import com.hermandad.repository.CuotaRepository;
import com.hermandad.repository.HermanoRepository;
import org.springframework.stereotype.Service;

@Service
public class CuotaService {

    private final CuotaRepository cuotaRepository;
    private final HermanoRepository hermanoRepository;

    public CuotaService(
            CuotaRepository cuotaRepository,
            HermanoRepository hermanoRepository) {

        this.cuotaRepository = cuotaRepository;
        this.hermanoRepository = hermanoRepository;
    }

    public Cuota guardar(
            Long hermanoId,
            Cuota cuota) {

        Hermano hermano =
                hermanoRepository.findById(hermanoId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Hermano no encontrado"));

        cuota.setHermano(hermano);

        cuota.setEstado(EstadoCuota.PENDIENTE);

        cuota.setFechaPago(null);

        return cuotaRepository.save(cuota);
    }
}