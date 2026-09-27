package com.hermandad.service;

import com.hermandad.dto.DashboardDto;
import com.hermandad.entity.Configuracion;
import com.hermandad.entity.EstadoCuota;
import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.TipoSocio;
import com.hermandad.repository.CuotaRepository;
import com.hermandad.repository.SocioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class DashboardService {

    private final SocioRepository socioRepository;
    private final CuotaRepository cuotaRepository;
    private final ConfiguracionService configuracionService;

    public DashboardService(SocioRepository socioRepository,
                            CuotaRepository cuotaRepository,
                            ConfiguracionService configuracionService) {

        this.socioRepository = socioRepository;
        this.cuotaRepository = cuotaRepository;
        this.configuracionService = configuracionService;
    }

    public DashboardDto obtenerDashboard() {

        Configuracion configuracion =
                configuracionService.obtenerConfiguracion();

        Integer anio = configuracion.getAnioActivo();

        DashboardDto dto = new DashboardDto();


        dto.setTotalSocios(socioRepository.countByEstado(EstadoSocio.ACTIVO));
        dto.setTotalHermanos(socioRepository.countByEstadoAndTipo(
                EstadoSocio.ACTIVO, TipoSocio.HERMANO));
        dto.setTotalCostaleros(socioRepository.countByEstadoAndTipo(
                EstadoSocio.ACTIVO, TipoSocio.COSTALERO));

        dto.setCuotasPagadas(cuotaRepository.countByEstadoAndAnio(EstadoCuota.PAGADA,anio));

        dto.setCuotasPendientes(cuotaRepository.countByEstadoAndAnio(EstadoCuota.PENDIENTE,anio));

        dto.setImporteRecaudado(Optional.ofNullable(cuotaRepository.sumImporteByEstadoAndAnio(EstadoCuota.PAGADA,anio)).orElse(BigDecimal.ZERO));

        dto.setImportePendiente(Optional.ofNullable(cuotaRepository.sumImporteByEstadoAndAnio(EstadoCuota.PENDIENTE,anio)).orElse(BigDecimal.ZERO));

        dto.setMorosos(cuotaRepository.contarMorosos(anio));

        dto.setAnioActivo(anio);

        Long cuotasPagadas=cuotaRepository.countByEstadoAndAnio(EstadoCuota.PAGADA,anio);
        Long cuotasPendientes=cuotaRepository.countByEstadoAndAnio(EstadoCuota.PENDIENTE,anio);

        dto.setTotalCuotas(cuotasPagadas + cuotasPendientes);

        if (dto.getTotalCuotas() > 0) {

            int porcentaje = (int) Math.round(
                    (dto.getCuotasPagadas() * 100.0) / dto.getTotalCuotas());

            dto.setPorcentajeCobrado(porcentaje);

        } else {

            dto.setPorcentajeCobrado(0);

        }


        return dto;
    }

}
