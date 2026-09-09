package com.hermandad.service;

import com.hermandad.dto.CartaMorosoDto;
import com.hermandad.dto.DashboardTesoreriaDto;
import com.hermandad.dto.MorosoDto;
import com.hermandad.dto.ResumenCuotasDto;
import com.hermandad.entity.Cuota;
import com.hermandad.entity.EstadoCuota;
import com.hermandad.entity.EstadoHermano;
import com.hermandad.entity.Hermano;
import com.hermandad.exception.CampoOrdenacionInvalidoException;
import com.hermandad.exception.RecursoNoEncontradoException;
import com.hermandad.repository.CuotaRepository;
import com.hermandad.repository.HermanoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CuotaService {

    private final CuotaRepository cuotaRepository;

    private final HermanoRepository hermanoRepository;

    private final AuditoriaService auditoriaService;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "anio",
            "importe",
            "estado",
            "fechaPago",
            "hermano.numeroHermano"
    );

    public CuotaService(
            CuotaRepository cuotaRepository,
            HermanoRepository hermanoRepository,
            AuditoriaService auditoriaService) {

        this.cuotaRepository = cuotaRepository;
        this.hermanoRepository = hermanoRepository;
        this.auditoriaService = auditoriaService;
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

        Cuota guardada =
                cuotaRepository.save(cuota);

        auditoriaService.registrar(
                "CREAR",
                "CUOTA",
                guardada.getId());

        return guardada;
    }

    public List<Cuota> obtenerPorHermano(
            Long hermanoId) {

        return cuotaRepository.findByHermanoId(
                hermanoId);
    }

    public Cuota pagar(Long id) {

        Cuota cuota = cuotaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cuota no encontrada"));

        cuota.setEstado(EstadoCuota.PAGADA);

        cuota.setFechaPago(LocalDate.now());

        Cuota actualizada =
                cuotaRepository.save(cuota);

        auditoriaService.registrar(
                "PAGAR",
                "CUOTA",
                actualizada.getId());

        return actualizada;
    }

    public Cuota anular(Long id) {

        Cuota cuota = cuotaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cuota no encontrada"));

        cuota.setEstado(EstadoCuota.ANULADA);

        cuota.setFechaPago(null);

        Cuota actualizada =
                cuotaRepository.save(cuota);

        auditoriaService.registrar(
                "ANULAR",
                "CUOTA",
                actualizada.getId());

        return actualizada;
    }

    public List<Cuota> obtenerPendientes() {

        return cuotaRepository.findByEstado(
                EstadoCuota.PENDIENTE);
    }

    public int generarCuotasAnuales(
            Integer anio,
            BigDecimal importe) {

        List<Hermano> hermanos =
                hermanoRepository.findByEstado(
                        EstadoHermano.ACTIVO);

        int creadas = 0;

        for (Hermano hermano : hermanos) {

            boolean existe =
                    cuotaRepository
                            .existsByHermanoIdAndAnio(
                                    hermano.getId(),
                                    anio);

            if (!existe) {

                Cuota cuota = new Cuota();

                cuota.setHermano(hermano);
                cuota.setAnio(anio);
                cuota.setImporte(importe);

                cuota.setEstado(
                        EstadoCuota.PENDIENTE);

                cuotaRepository.save(cuota);

                creadas++;
            }
        }

        auditoriaService.registrar(
                "GENERAR_ANUALES",
                "CUOTA",
                null);

        return creadas;
    }

    public ResumenCuotasDto obtenerResumen() {

        ResumenCuotasDto dto =
                new ResumenCuotasDto();

        dto.setTotalCuotas(
                cuotaRepository.count());

        dto.setPendientes(
                cuotaRepository.countByEstado(
                        EstadoCuota.PENDIENTE));

        dto.setPagadas(
                cuotaRepository.countByEstado(
                        EstadoCuota.PAGADA));

        dto.setAnuladas(
                cuotaRepository.countByEstado(
                        EstadoCuota.ANULADA));

        dto.setImportePendiente(
                cuotaRepository.findByEstado(
                                EstadoCuota.PENDIENTE)
                        .stream()
                        .map(Cuota::getImporte)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add));

        dto.setImporteCobrado(
                cuotaRepository.findByEstado(
                                EstadoCuota.PAGADA)
                        .stream()
                        .map(Cuota::getImporte)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add));

        return dto;
    }

    public List<MorosoDto> obtenerMorosos() {

        List<Cuota> cuotasPendientes =
                cuotaRepository.findByEstado(
                        EstadoCuota.PENDIENTE);

        Map<Long, List<Cuota>> agrupadas =
                cuotasPendientes.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getHermano().getId()));

        List<MorosoDto> resultado =
                new ArrayList<>();

        for (List<Cuota> cuotas : agrupadas.values()) {

            Hermano hermano =
                    cuotas.getFirst().getHermano();

            MorosoDto dto =
                    new MorosoDto();

            dto.setHermanoId(
                    hermano.getId());

            dto.setNumeroHermano(
                    hermano.getNumeroHermano());

            dto.setNombreCompleto(
                    hermano.getNombre()
                            + " "
                            + hermano.getApellidos());

            dto.setCuotasPendientes(
                    (long) cuotas.size());

            dto.setImportePendiente(
                    cuotas.stream()
                            .map(Cuota::getImporte)
                            .reduce(
                                    BigDecimal.ZERO,
                                    BigDecimal::add));

            resultado.add(dto);
        }

        return resultado;
    }

    public CartaMorosoDto obtenerCartaMoroso(
            Long hermanoId) {
        List<Cuota> cuotas =
                cuotaRepository.findByHermanoId(
                                hermanoId)
                        .stream()
                        .filter(c ->
                                c.getEstado()
                                        == EstadoCuota.PENDIENTE)
                        .toList();

        if (cuotas.isEmpty()) {

            throw new RecursoNoEncontradoException(
                    "El hermano no tiene cuotas pendientes");
        }

        Hermano hermano =
                cuotas.get(0).getHermano();

        CartaMorosoDto dto =
                new CartaMorosoDto();

        dto.setNombreCompleto(
                hermano.getNombre()
                        + " "
                        + hermano.getApellidos());

        dto.setCuotasPendientes(
                (long) cuotas.size());

        dto.setImportePendiente(
                cuotas.stream()
                        .map(Cuota::getImporte)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add));

        return dto;
    }

    public DashboardTesoreriaDto obtenerDashboard() {

        DashboardTesoreriaDto dto =
                new DashboardTesoreriaDto();

        dto.setTotalHermanos(
                hermanoRepository.count());

        dto.setCuotasPagadas(
                cuotaRepository.countByEstado(
                        EstadoCuota.PAGADA));

        dto.setCuotasPendientes(
                cuotaRepository.countByEstado(
                        EstadoCuota.PENDIENTE));

        dto.setImporteRecaudado(
                cuotaRepository.totalRecaudado());

        dto.setImportePendiente(
                cuotaRepository.totalPendiente());

        dto.setMorosos(
                (long) obtenerMorosos().size());

        return dto;
    }
    public List<Cuota> obtenerTodas() {

        return cuotaRepository.findAll();
    }

    public Cuota obtenerPorId(Long id) {

        return cuotaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Cuota no encontrada"));
    }

    public Cuota actualizar(
            Long id,
            Cuota datos) {

        Cuota cuota =
                cuotaRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Cuota no encontrada"));

        cuota.setImporte(datos.getImporte());
        cuota.setObservaciones(datos.getObservaciones());

        Cuota actualizada =
                cuotaRepository.save(cuota);

        auditoriaService.registrar(
                "MODIFICAR",
                "CUOTA",
                actualizada.getId());

        return actualizada;
    }

    public void eliminar(Long id) {

        Cuota cuota =
                cuotaRepository.findById(id)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Cuota no encontrada"));

        auditoriaService.registrar(
                "ELIMINAR",
                "CUOTA",
                cuota.getId());

        cuotaRepository.delete(cuota);
    }

    public Page<Cuota> obtenerPaginadas(
            int page,
            int size,
            String sort,
            String direction) {

        if (!CAMPOS_ORDENABLES.contains(sort)) {
            throw new CampoOrdenacionInvalidoException(
                    "Campo de ordenación no permitido: " + sort);
        }

        Sort.Direction direccion =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direccion, sort));

        return cuotaRepository.findAll(pageable);
    }

    public Page<Cuota> buscarPaginado(
            String texto,
            EstadoCuota estado,
            Integer anio,
            int page,
            int size,
            String sort,
            String direction) {

        if (texto == null) {
            texto = "";
        }

        if (!CAMPOS_ORDENABLES.contains(sort)) {
            throw new CampoOrdenacionInvalidoException(
                    "Campo de ordenación no permitido: " + sort);
        }

        Sort.Direction direccion =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direccion, sort));

        return cuotaRepository.buscar(
                texto,
                estado,
                anio,
                pageable);
    }

    public List<Integer> obtenerAniosDisponibles() {
        return cuotaRepository.obtenerAniosDisponibles();
    }
}