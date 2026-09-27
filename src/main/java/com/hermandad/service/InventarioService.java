package com.hermandad.service;

import com.hermandad.dto.InventarioRequestDto;
import com.hermandad.dto.RevisionInventarioDetalleRequestDto;
import com.hermandad.dto.RevisionInventarioRequestDto;
import com.hermandad.entity.*;
import com.hermandad.exception.BusinessException;
import com.hermandad.mapper.InventarioMapper;
import com.hermandad.repository.ElementoInventarioRepository;
import com.hermandad.repository.RevisionInventarioDetalleRepository;
import com.hermandad.repository.RevisionInventarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventarioService {

    private final ElementoInventarioRepository elementoRepository;
    private final RevisionInventarioRepository revisionRepository;
    private final RevisionInventarioDetalleRepository detalleRepository;
    private final InventarioMapper inventarioMapper;
    private final AuditoriaService auditoriaService;

    public InventarioService(
            ElementoInventarioRepository elementoRepository,
            RevisionInventarioRepository revisionRepository,
            RevisionInventarioDetalleRepository detalleRepository,
            InventarioMapper inventarioMapper,
            AuditoriaService auditoriaService) {
        this.elementoRepository = elementoRepository;
        this.revisionRepository = revisionRepository;
        this.detalleRepository = detalleRepository;
        this.inventarioMapper = inventarioMapper;
        this.auditoriaService = auditoriaService;
    }

    public Page<ElementoInventario> buscar(String texto, String categoria,
                                             EstadoInventario estado, Pageable pageable) {
        return elementoRepository.buscar(normalizar(texto), normalizar(categoria), estado, pageable);
    }

    public ElementoInventario obtenerElemento(Long id) {
        return elementoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("El bien inventariado no existe."));
    }

    @Transactional
    public ElementoInventario crearElemento(InventarioRequestDto dto) {
        String codigo = dto.getCodigo().trim();
        if (elementoRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new BusinessException("Ya existe un bien con ese código.");
        }

        ElementoInventario elemento = new ElementoInventario();
        inventarioMapper.actualizarEntidad(dto, elemento);
        elemento.setActivo(true);
        ElementoInventario guardado = elementoRepository.save(elemento);
        auditoriaService.registrar("CREAR", "INVENTARIO", guardado.getId());
        return guardado;
    }

    @Transactional
    public ElementoInventario actualizarElemento(Long id, InventarioRequestDto dto) {
        ElementoInventario elemento = obtenerElemento(id);
        String codigo = dto.getCodigo().trim();
        if (elementoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new BusinessException("Ya existe un bien con ese código.");
        }

        inventarioMapper.actualizarEntidad(dto, elemento);
        ElementoInventario guardado = elementoRepository.save(elemento);
        auditoriaService.registrar("MODIFICAR", "INVENTARIO", guardado.getId());
        return guardado;
    }

    @Transactional
    public ElementoInventario cambiarActivo(Long id, boolean activo) {
        ElementoInventario elemento = obtenerElemento(id);
        elemento.setActivo(activo);
        ElementoInventario guardado = elementoRepository.save(elemento);
        auditoriaService.registrar(activo ? "ACTIVAR" : "DESACTIVAR", "INVENTARIO", id);
        return guardado;
    }

    @Transactional
    public RevisionInventario crearRevision(RevisionInventarioRequestDto dto) {
        List<ElementoInventario> elementos = elementoRepository.findByActivoTrue();
        if (elementos.isEmpty()) {
            throw new BusinessException("No hay bienes activos para auditar.");
        }

        RevisionInventario revision = new RevisionInventario();
        revision.setTitulo(dto.getTitulo().trim());
        revision.setObservaciones(vacioANull(dto.getObservaciones()));
        revision.setFecha(LocalDateTime.now());
        revision.setUsuario(usuarioActual());
        revision.setEstado(EstadoRevisionInventario.ABIERTA);
        RevisionInventario guardada = revisionRepository.save(revision);

        for (ElementoInventario elemento : elementos) {
            RevisionInventarioDetalle detalle = new RevisionInventarioDetalle();
            detalle.setRevision(guardada);
            detalle.setElemento(elemento);
            detalle.setVerificado(false);
            detalleRepository.save(detalle);
        }

        auditoriaService.registrar("CREAR", "REVISION_INVENTARIO", guardada.getId());
        return guardada;
    }

    public List<RevisionInventario> obtenerRevisiones() {
        return revisionRepository.findAllByOrderByFechaDesc();
    }

    public RevisionInventario obtenerRevision(Long id) {
        return revisionRepository.findById(id)
                .orElseThrow(() -> new BusinessException("La revisión de inventario no existe."));
    }

    public List<RevisionInventarioDetalle> obtenerDetalles(Long revisionId) {
        obtenerRevision(revisionId);
        return detalleRepository.findByRevisionIdOrderByElementoCodigoAsc(revisionId);
    }

    @Transactional
    public RevisionInventarioDetalle actualizarDetalle(Long detalleId,
                                                       RevisionInventarioDetalleRequestDto dto) {
        RevisionInventarioDetalle detalle = detalleRepository.findById(detalleId)
                .orElseThrow(() -> new BusinessException("El detalle de revisión no existe."));
        if (detalle.getRevision().getEstado() == EstadoRevisionInventario.CERRADA) {
            throw new BusinessException("No se puede modificar una revisión cerrada.");
        }

        detalle.setVerificado(dto.getVerificado());
        detalle.setEstadoObservado(dto.getEstadoObservado());
        detalle.setUbicacionObservada(vacioANull(dto.getUbicacionObservada()));
        detalle.setIncidencia(vacioANull(dto.getIncidencia()));
        detalle.setFechaVerificacion(dto.getVerificado() ? LocalDateTime.now() : null);
        RevisionInventarioDetalle guardado = detalleRepository.save(detalle);
        auditoriaService.registrar("MODIFICAR", "REVISION_INVENTARIO_DETALLE", guardado.getId());
        return guardado;
    }

    @Transactional
    public RevisionInventario cerrarRevision(Long id) {
        RevisionInventario revision = obtenerRevision(id);
        if (revision.getEstado() == EstadoRevisionInventario.CERRADA) {
            return revision;
        }
        revision.setEstado(EstadoRevisionInventario.CERRADA);
        RevisionInventario guardada = revisionRepository.save(revision);
        auditoriaService.registrar("CERRAR", "REVISION_INVENTARIO", id);
        return guardada;
    }

    public long totalDetalles(Long revisionId) {
        return detalleRepository.countByRevisionId(revisionId);
    }

    public long totalVerificados(Long revisionId) {
        return detalleRepository.countByRevisionIdAndVerificadoTrue(revisionId);
    }

    public long totalIncidencias(Long revisionId) {
        return detalleRepository.countByRevisionIdAndIncidenciaIsNotNull(revisionId);
    }

    private String usuarioActual() {
        return SecurityContextHolder.getContext().getAuthentication() != null
                ? SecurityContextHolder.getContext().getAuthentication().getName()
                : "SISTEMA";
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    private String vacioANull(String valor) {
        String normalizado = normalizar(valor);
        return normalizado.isEmpty() ? null : normalizado;
    }
}
