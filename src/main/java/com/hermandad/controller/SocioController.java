package com.hermandad.controller;

import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.Socio;
import com.hermandad.entity.TipoSocio;
import com.hermandad.service.SocioService;
import com.hermandad.report.ExcelService;

import com.hermandad.dto.SocioRequestDto;
import com.hermandad.dto.SocioResponseDto;
import com.hermandad.mapper.SocioMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

@Tag(name = "Socios")
@RestController
@RequestMapping("/api/socios")
public class SocioController {
    private final SocioMapper socioMapper;
    private final SocioService socioService;
    private final ExcelService excelService;

    public SocioController(
            SocioService socioService,
            SocioMapper socioMapper,
            ExcelService excelService) {

        this.socioService = socioService;
        this.socioMapper = socioMapper;
        this.excelService = excelService;
    }

    @GetMapping("/exportar-excel")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public ResponseEntity<byte[]> exportarExcel(@RequestParam(required = false) String texto,
                                                @RequestParam(required = false) EstadoSocio estado,
                                                @RequestParam(required = false) TipoSocio tipo) throws Exception {
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=socios.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelService.exportarSocios(socioService.buscarPaginado(texto, estado, tipo, 0, 10000, "numeroSocio", "asc").getContent()));
    }

    @Operation(summary = "Obtener todos los socios")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public List<SocioResponseDto> obtenerTodos() {

        return socioService.obtenerTodos()
                .stream()
                .map(socioMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Obtener socio por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public SocioResponseDto obtenerPorId(
            @PathVariable Long id) {

        Socio socio = socioService.obtenerPorId(id);

        return socioMapper.toResponse(socio);
    }

    @Operation(summary = "Obtener socio por DNI")
    @GetMapping("/dni/{dni}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public SocioResponseDto buscarPorDni(
            @PathVariable String dni) {

        Socio socio = socioService.buscarPorDni(dni);

        return socioMapper.toResponse(socio);
    }

    @Operation(summary = "Obtener socio por estado")
    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public List<SocioResponseDto> buscarPorEstado(
            @PathVariable EstadoSocio estado) {

        return socioService.buscarPorEstado(estado)
                .stream()
                .map(socioMapper::toResponse)
                .toList();
    }

    @Operation(summary = "Obtener socio por apellidos")
    @GetMapping("/apellidos/{apellidos}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public List<SocioResponseDto> buscarPorApellidos(
            @PathVariable String apellidos) {

        return socioService.buscarPorApellidos(apellidos)
                .stream()
                .map(socioMapper::toResponse)
                .toList();
    }

    @GetMapping("/paginado")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public Page<SocioResponseDto> obtenerPaginados(

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size,

            @RequestParam(defaultValue = "numeroSocio")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return socioService
                .obtenerPaginados(
                        page,
                        size,
                        sort,
                        direction)
                .map(socioMapper::toResponse);
    }

    @GetMapping("/busqueda-paginada")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public Page<SocioResponseDto> buscarPaginado(

            @RequestParam(required = false)
            String texto,

            @RequestParam(required = false)
            EstadoSocio estado,

            @RequestParam(required = false)
            TipoSocio tipo,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "numeroSocio")
            String sort,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return socioService
                .buscarPaginado(
                        texto,
                        estado,
                        tipo,
                        page,
                        size,
                        sort,
                        direction)
                .map(socioMapper::toResponse);
    }

    @GetMapping("/domiciliados")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO','SECRETARIO','CONSULTA')")
    public List<SocioResponseDto> domiciliados() {

        return socioService
                .obtenerDomiciliados()
                .stream()
                .map(socioMapper::toResponse)
                .toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public SocioResponseDto crear(
            @Valid @RequestBody SocioRequestDto dto) {

        Socio socio = socioMapper.toEntity(dto);

        Socio guardado = socioService.guardar(socio);

        return socioMapper.toResponse(guardado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public SocioResponseDto actualizar(
            @PathVariable Long id,
            @Valid @RequestBody SocioRequestDto dto) {

        Socio socio = socioMapper.toEntity(dto);

        Socio actualizado =
                socioService.actualizar(id, socio);

        return socioMapper.toResponse(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIO')")
    public void eliminar(@PathVariable Long id) {

        socioService.eliminar(id);
    }
}
