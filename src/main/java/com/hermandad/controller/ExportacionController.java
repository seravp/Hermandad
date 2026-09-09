package com.hermandad.controller;

import com.hermandad.report.ExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExportacionController {

    private final ExcelService excelService;

    public ExportacionController(
            ExcelService excelService) {

        this.excelService = excelService;
    }

    @GetMapping("/api/exportaciones/hermanos")
    @PreAuthorize(
            "hasAnyRole('ADMIN','SECRETARIO')")
    public ResponseEntity<byte[]>
    exportarHermanos()
            throws Exception {

        byte[] excel =
                excelService.exportarHermanos();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=hermanos.xlsx")
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }

    @GetMapping("/api/exportaciones/morosos")
    @PreAuthorize(
            "hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]>
    exportarMorosos()
            throws Exception {

        byte[] excel =
                excelService.exportarMorosos();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=morosos.xlsx")
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }

    @GetMapping("/api/exportaciones/cuotas")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> exportarCuotas()
            throws Exception {

        byte[] excel =
                excelService.exportarCuotas();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=cuotas.xlsx")
                .header(
                        HttpHeaders.CONTENT_TYPE,
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }
}