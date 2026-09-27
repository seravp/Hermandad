package com.hermandad.controller;

import com.hermandad.report.InformeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.hermandad.report.ExcelService;

@RestController
public class InformeController {

    private final InformeService informeService;
    private final ExcelService excelService;

    public InformeController(
            InformeService informeService,
            ExcelService excelService) {

        this.informeService = informeService;
        this.excelService = excelService;
    }

    @GetMapping("/api/informes/prueba")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> prueba()
            throws Exception {

        byte[] pdf =
                informeService.generarPdfPrueba();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=prueba.pdf")
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/api/informes/morosos")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> morosos()
            throws Exception {

        byte[] pdf =
                informeService
                        .generarInformeMorosos();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=morosos.pdf")
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/api/informes/domiciliados")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> domiciliados()
            throws Exception {

        byte[] pdf =
                informeService
                        .generarInformeDomiciliados();

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=domiciliados.pdf")
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping(
            "/api/informes/carta-moroso/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> cartaMoroso(
            @PathVariable Long id)
            throws Exception {

        byte[] pdf =
                informeService
                        .generarCartaMoroso(id);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=carta-moroso.pdf")
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/api/informes/excel/socios")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> excelSocios()
            throws Exception {

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=socios.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument."
                                + "spreadsheetml.sheet"))
                .body(excelService.exportarSocios());
    }

    @GetMapping("/api/informes/excel/morosos")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> excelMorosos()
            throws Exception {

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=morosos.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument."
                                + "spreadsheetml.sheet"))
                .body(excelService.exportarMorosos());
    }

    @GetMapping("/api/informes/excel/cuotas")
    @PreAuthorize("hasAnyRole('ADMIN','TESORERO')")
    public ResponseEntity<byte[]> excelCuotas()
            throws Exception {

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=cuotas.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument."
                                + "spreadsheetml.sheet"))
                .body(excelService.exportarCuotas());
    }
}