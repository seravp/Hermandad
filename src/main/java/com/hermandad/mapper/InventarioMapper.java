package com.hermandad.mapper;

import com.hermandad.dto.InventarioRequestDto;
import com.hermandad.dto.InventarioResponseDto;
import com.hermandad.dto.RevisionInventarioDetalleResponseDto;
import com.hermandad.dto.RevisionInventarioResponseDto;
import com.hermandad.entity.ElementoInventario;
import com.hermandad.entity.RevisionInventario;
import com.hermandad.entity.RevisionInventarioDetalle;
import org.springframework.stereotype.Component;

@Component
public class InventarioMapper {

    public void actualizarEntidad(InventarioRequestDto dto, ElementoInventario elemento) {
        elemento.setCodigo(dto.getCodigo().trim());
        elemento.setNombre(dto.getNombre().trim());
        elemento.setCategoria(dto.getCategoria().trim());
        elemento.setDescripcion(dto.getDescripcion());
        elemento.setUbicacion(dto.getUbicacion().trim());
        elemento.setEstado(dto.getEstado());
        elemento.setFechaAdquisicion(dto.getFechaAdquisicion());
        elemento.setValorAdquisicion(dto.getValorAdquisicion());
        elemento.setObservaciones(dto.getObservaciones());
    }

    public InventarioResponseDto toResponse(ElementoInventario elemento) {
        InventarioResponseDto dto = new InventarioResponseDto();
        dto.setId(elemento.getId());
        dto.setCodigo(elemento.getCodigo());
        dto.setNombre(elemento.getNombre());
        dto.setCategoria(elemento.getCategoria());
        dto.setDescripcion(elemento.getDescripcion());
        dto.setUbicacion(elemento.getUbicacion());
        dto.setEstado(elemento.getEstado());
        dto.setFechaAdquisicion(elemento.getFechaAdquisicion());
        dto.setValorAdquisicion(elemento.getValorAdquisicion());
        dto.setActivo(elemento.getActivo());
        dto.setObservaciones(elemento.getObservaciones());
        dto.setImagenUrl(elemento.getImagenUrl());
        return dto;
    }

    public RevisionInventarioResponseDto toRevisionResponse(
            RevisionInventario revision, long total, long verificados, long incidencias) {
        RevisionInventarioResponseDto dto = new RevisionInventarioResponseDto();
        dto.setId(revision.getId());
        dto.setTitulo(revision.getTitulo());
        dto.setFecha(revision.getFecha());
        dto.setUsuario(revision.getUsuario());
        dto.setEstado(revision.getEstado());
        dto.setObservaciones(revision.getObservaciones());
        dto.setTotalElementos(total);
        dto.setVerificados(verificados);
        dto.setIncidencias(incidencias);
        return dto;
    }

    public RevisionInventarioDetalleResponseDto toDetalleResponse(RevisionInventarioDetalle detalle) {
        RevisionInventarioDetalleResponseDto dto = new RevisionInventarioDetalleResponseDto();
        dto.setId(detalle.getId());
        dto.setElementoId(detalle.getElemento().getId());
        dto.setCodigo(detalle.getElemento().getCodigo());
        dto.setNombre(detalle.getElemento().getNombre());
        dto.setCategoria(detalle.getElemento().getCategoria());
        dto.setUbicacionRegistrada(detalle.getElemento().getUbicacion());
        dto.setEstadoRegistrado(detalle.getElemento().getEstado());
        dto.setVerificado(detalle.getVerificado());
        dto.setEstadoObservado(detalle.getEstadoObservado());
        dto.setUbicacionObservada(detalle.getUbicacionObservada());
        dto.setIncidencia(detalle.getIncidencia());
        dto.setFechaVerificacion(detalle.getFechaVerificacion());
        return dto;
    }
}
