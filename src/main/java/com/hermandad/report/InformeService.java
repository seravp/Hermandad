package com.hermandad.report;

import com.hermandad.dto.CartaMorosoDto;
import com.hermandad.dto.MorosoDto;
import com.hermandad.entity.Hermano;
import com.hermandad.service.CuotaService;
import com.hermandad.service.HermanoService;
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

    private final CuotaService cuotaService;

    private final HermanoService hermanoService;

    public InformeService(
            CuotaService cuotaService,
            HermanoService hermanoService) {

        this.cuotaService = cuotaService;
        this.hermanoService = hermanoService;
    }

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

    public byte[] generarInformeDomiciliados()
            throws IOException {

        List<Hermano> domiciliados =
                hermanoService.obtenerDomiciliados();

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
                        "LISTADO DE HERMANOS DOMICILIADOS");

                content.endText();

                // Datos

                float y = 700;

                for (Hermano hermano : domiciliados) {

                    content.beginText();

                    content.setFont(
                            new PDType1Font(
                                    Standard14Fonts.FontName.HELVETICA),
                            12);

                    content.newLineAtOffset(50, y);

                    content.showText(
                            hermano.getNumeroHermano()
                                    + " - "
                                    + hermano.getNombre()
                                    + " "
                                    + hermano.getApellidos()
                                    + " - "
                                    + hermano.getIban());

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

    public byte[] generarCartaMoroso(
            Long hermanoId)
            throws IOException {

        CartaMorosoDto carta =
                cuotaService.obtenerCartaMoroso(
                        hermanoId);

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage();

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(
                                 document,
                                 page)) {

                // Título

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD),
                        16);

                content.newLineAtOffset(50, 750);

                content.showText(
                        "CARTA DE CUOTAS PENDIENTES");

                content.endText();

                // Cuerpo

                float y = 700;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA),
                        12);

                content.newLineAtOffset(50, y);

                content.showText(
                        "Estimado hermano "
                                + carta.getNombreCompleto()
                                + ":");

                content.endText();

                y -= 40;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA),
                        12);

                content.newLineAtOffset(50, y);

                content.showText(
                        "Segun nuestros registros mantiene "
                                + carta.getCuotasPendientes()
                                + " cuotas pendientes.");

                content.endText();

                y -= 20;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA),
                        12);

                content.newLineAtOffset(50, y);

                content.showText(
                        "Importe pendiente: "
                                + carta.getImportePendiente()
                                + " EUR");

                content.endText();

                y -= 40;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA),
                        12);

                content.newLineAtOffset(50, y);

                content.showText(
                        "Le rogamos regularice su situacion a la mayor brevedad posible.");

                content.endText();

                y -= 60;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD),
                        12);

                content.newLineAtOffset(50, y);

                content.showText(
                        "Tesoreria");

                content.endText();
            }

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            document.save(baos);

            return baos.toByteArray();
        }
    }

}