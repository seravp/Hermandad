package com.hermandad.report;

import com.hermandad.dto.MorosoDto;
import com.hermandad.service.CuotaService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

@Service
public class InformeService {

    public byte[] generarPdfPrueba() throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page)) {

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA),
                        12);

                content.newLineAtOffset(50, 700);

                content.showText(
                        "Informe de prueba Hermandad");

                content.endText();
            }

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            document.save(baos);

            return baos.toByteArray();
        }
    }

    private final CuotaService cuotaService;

    public InformeService(
            CuotaService cuotaService) {

        this.cuotaService = cuotaService;
    }

    public byte[] generarInformeMorosos()
            throws IOException {

        List<MorosoDto> morosos =
                cuotaService.obtenerMorosos();

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page)) {

                // Cabecera

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD),
                        16);

                content.newLineAtOffset(50, 750);

                content.showText(
                        "INFORME DE MOROSOS");

                content.endText();

                // Líneas

                float y = 700;

                for (MorosoDto moroso : morosos) {

                    content.beginText();

                    content.setFont(
                            new PDType1Font(
                                    Standard14Fonts.FontName.HELVETICA),
                            12);

                    content.newLineAtOffset(50, y);

                    content.showText(
                            moroso.getNumeroHermano()
                                    + " - "
                                    + moroso.getNombreCompleto()
                                    + " - Cuotas: "
                                    + moroso.getCuotasPendientes()
                                    + " - "
                                    + moroso.getImportePendiente()
                                    + " €");

                    content.endText();

                    y -= 20;
                }
            }

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            document.save(baos);

            return baos.toByteArray();
        }
    }
}