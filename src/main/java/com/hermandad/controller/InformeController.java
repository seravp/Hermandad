package com.hermandad.controller;

import com.hermandad.report.InformeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InformeController {

    private final InformeService informeService;

    public InformeController(
            InformeService informeService) {

        this.informeService = informeService;
    }

    @GetMapping("/api/informes/prueba")
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
}