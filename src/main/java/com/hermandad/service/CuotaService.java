package com.hermandad.service;

import com.hermandad.dto.CartaMorosoDto;
import com.hermandad.dto.DashboardDto;
import com.hermandad.dto.MorosoDto;
import com.hermandad.dto.ResumenCuotasDto;
import com.hermandad.entity.*;
import com.hermandad.exception.BusinessException;
import com.hermandad.exception.CampoOrdenacionInvalidoException;
import com.hermandad.exception.RecursoNoEncontradoException;
import com.hermandad.repository.CuotaRepository;
import com.hermandad.repository.SocioRepository;
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
    private final SocioRepository socioRepository;
    private final AuditoriaService auditoriaService;
    private final ConfiguracionService configuracionService;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "anio",
            "importe",
            "estado",
            "fechaPago",
            "numeroSocio"
    );

    public CuotaService(
            CuotaRepository cuotaRepository,
            SocioRepository socioRepository,
            AuditoriaService auditoriaService,
            ConfiguracionService configuracionService) {

        this.cuotaRepository = cuotaRepository;
        this.socioRepository = socioRepository;
        this.auditoriaService = auditoriaService;
        this.configuracionService = configuracionService;
    }

    public Cuota guardar(
            Long socioId,
            Cuota cuota) {

        if (socioId == null) {
            throw new BusinessException(
                    "Debe seleccionarse un socio.");
        }

        if (cuota.getAnio() == null) {
            throw new BusinessException(
                    "El año de la cuota es obligatorio.");
        }

        if (cuota.getImporte() == null
                || cuota.getImporte()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "El importe debe ser mayor que cero.");
        }

        if (cuotaRepository
                .existsBySocioIdAndAnio(
                        socioId,
                        cuota.getAnio())) {

            throw new BusinessException(
                    "El socio ya tiene una cuota para el año "
                            + cuota.getAnio() + ".");
        }

        Socio socio =
                socioRepository.findById(socioId)
                        .orElseThrow(() ->
                                new RecursoNoEncontradoException(
                                        "Socio no encontrado"));

        cuota.setSocio(socio);
        cuota.setTipo(socio.getTipo());
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

    public List<Cuota> obtenerPorSocio(
            Long socioId) {

        return cuotaRepository.findBySocioId(
                socioId);
    }

    public Cuota pagar(Long id) {

        Cuota cuota = obtenerCuota(id);

        if (cuota.getEstado() == EstadoCuota.PAGADA) {
            throw new BusinessException("La cuota ya está pagada.");
        }

        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new BusinessException("No se puede marcar como pagada una cuota anulada.");
        }

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

        Cuota cuota = obtenerCuota(id);

        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new BusinessException("La cuota ya está anulada.");
        }

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

    public Cuota deshacerPago(Long id) {

        Cuota cuota = obtenerCuota(id);

        if (cuota.getEstado() == EstadoCuota.PENDIENTE) {
            throw new BusinessException("La cuota ya está pendiente.");
        }

        if (cuota.getEstado() != EstadoCuota.PAGADA) {
            throw new BusinessException("Solo se pueden deshacer cuotas pagadas.");
        }

        cuota.setEstado(EstadoCuota.PENDIENTE);

        cuota.setFechaPago(null);

        Cuota actualizada =
                cuotaRepository.save(cuota);

        auditoriaService.registrar(
                "DESHACER-PAGO",
                "CUOTA",
                actualizada.getId());

        return actualizada;
    }

    public List<Cuota> obtenerPendientes() {

        return cuotaRepository.findByEstado(
                EstadoCuota.PENDIENTE);
    }

    public int generarCuotasAnuales(Integer anio) {


        Configuracion configuracion = configuracionService.obtenerConfiguracion();

        List<Socio> socios =
                socioRepository.findByEstado(
                        EstadoSocio.ACTIVO);

        int creadas = 0;

        for (Socio socio : socios) {

            boolean existe =
                    cuotaRepository
                            .existsBySocioIdAndAnio(
                                    socio.getId(),
                                    anio);

            if (!existe) {

                Cuota cuota = new Cuota();

                cuota.setSocio(socio);
                cuota.setAnio(anio);
                cuota.setTipo(socio.getTipo());
                cuota.setImporte(obtenerImporte(configuracion, socio.getTipo()));

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
        return obtenerMorosos(null);
    }

    public List<MorosoDto> obtenerMorosos(Integer anio) {

        List<Cuota> cuotasPendientes =
                cuotaRepository.findByEstado(
                        EstadoCuota.PENDIENTE);

        if (anio != null) {
            cuotasPendientes = cuotasPendientes.stream()
                    .filter(cuota -> cuota.getAnio().equals(anio))
                    .toList();
        }

        Map<Long, List<Cuota>> agrupadas =
                cuotasPendientes.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getSocio().getId()));

        List<MorosoDto> resultado =
                new ArrayList<>();

        for (List<Cuota> cuotas : agrupadas.values()) {

            Socio socio =
                    cuotas.getFirst().getSocio();

            MorosoDto dto =
                    new MorosoDto();

            dto.setSocioId(
                    socio.getId());

            dto.setNumeroSocio(
                    socio.getNumeroSocio());

            dto.setNombreCompleto(
                    socio.getNombre()
                            + " "
                            + socio.getApellidos());

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
            Long socioId) {
        List<Cuota> cuotas =
                cuotaRepository.findBySocioId(
                                socioId)
                        .stream()
                        .filter(c ->
                                c.getEstado()
                                        == EstadoCuota.PENDIENTE)
                        .toList();

        if (cuotas.isEmpty()) {

            throw new RecursoNoEncontradoException(
                    "El socio no tiene cuotas pendientes");
        }

        Socio socio =
                cuotas.get(0).getSocio();

        CartaMorosoDto dto =
                new CartaMorosoDto();

        dto.setNombreCompleto(
                socio.getNombre()
                        + " "
                        + socio.getApellidos());

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

    public DashboardDto obtenerDashboard() {

        DashboardDto dto =
                new DashboardDto();

        dto.setTotalSocios(
                socioRepository.count());

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

        Cuota cuota = obtenerCuota(id);

        if (cuota.getEstado() == EstadoCuota.ANULADA) {
            throw new BusinessException(
                    "No se puede editar una cuota anulada.");
        }

        if (datos.getImporte() == null
                || datos.getImporte()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "El importe debe ser mayor que cero.");
        }

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

        Cuota cuota = obtenerCuota(id);

        if (cuota.getEstado() == EstadoCuota.PAGADA) {
            throw new BusinessException(
                    "No se puede eliminar una cuota pagada."
            );
        }

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

        String campoOrden = switch (sort) {

            case "numeroSocio" -> "socio.numeroSocio";

            case "nombreSocio" -> "socio.nombre";

            default -> sort;

        };

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direccion, campoOrden));

        return cuotaRepository.findAll(pageable);
    }

    public Page<Cuota> buscarPaginado(
            String texto,
            EstadoCuota estado,
            Integer anio,
            TipoSocio tipo,
            String cuadrilla,
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

        String campoOrden = switch (sort) {
            case "numeroSocio" -> "socio.numeroSocio";
            case "nombreSocio" -> "socio.nombre";
            default -> sort;
        };

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direccion, campoOrden));

        return cuotaRepository.buscar(
                texto,
                estado,
                anio,
                tipo,
                cuadrilla,
                pageable);
    }

    public List<Integer> obtenerAniosDisponibles() {
        return cuotaRepository.obtenerAniosDisponibles();
    }

    private Cuota obtenerCuota(Long id) {
        return cuotaRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Cuota no encontrada"));
    }

    private BigDecimal obtenerImporte(Configuracion configuracion, TipoSocio tipo) {
        return tipo == TipoSocio.HERMANO
                ? configuracion.getImporteCuotaHermano()
                : configuracion.getImporteCuotaCostalero();
    }
}
