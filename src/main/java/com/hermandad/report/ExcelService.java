package com.hermandad.report;

import com.hermandad.dto.MorosoDto;
import com.hermandad.entity.Cuota;
import com.hermandad.entity.Hermano;
import com.hermandad.service.CuotaService;
import com.hermandad.service.HermanoService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ExcelService {

    private final HermanoService hermanoService;

    private final CuotaService cuotaService;

    public ExcelService(
            HermanoService hermanoService,
            CuotaService cuotaService) {

        this.hermanoService = hermanoService;
        this.cuotaService = cuotaService;
    }

    public byte[] exportarHermanos()
            throws Exception {

        List<Hermano> hermanos =
                hermanoService.obtenerTodos();

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Hermanos");

            Row cabecera =
                    sheet.createRow(0);

            cabecera.createCell(0)
                    .setCellValue("Número");

            cabecera.createCell(1)
                    .setCellValue("Nombre");

            cabecera.createCell(2)
                    .setCellValue("Apellidos");

            cabecera.createCell(3)
                    .setCellValue("DNI");

            cabecera.createCell(4)
                    .setCellValue("Estado");

            int fila = 1;

            for (Hermano hermano : hermanos) {

                Row row =
                        sheet.createRow(fila++);

                row.createCell(0)
                        .setCellValue(
                                hermano.getNumeroHermano());

                row.createCell(1)
                        .setCellValue(
                                hermano.getNombre());

                row.createCell(2)
                        .setCellValue(
                                hermano.getApellidos());

                row.createCell(3)
                        .setCellValue(
                                hermano.getDni());

                row.createCell(4)
                        .setCellValue(
                                hermano.getEstado() != null
                                        ? hermano.getEstado().name()
                                        : "");
            }

            for (int i = 0; i < 5; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            workbook.write(baos);

            return baos.toByteArray();
        }
    }

    public byte[] exportarMorosos()
            throws Exception {

        List<MorosoDto> morosos =
                cuotaService.obtenerMorosos();

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Morosos");

            Row cabecera =
                    sheet.createRow(0);

            cabecera.createCell(0)
                    .setCellValue("Número");

            cabecera.createCell(1)
                    .setCellValue("Nombre");

            cabecera.createCell(2)
                    .setCellValue("Cuotas Pendientes");

            cabecera.createCell(3)
                    .setCellValue("Importe Pendiente");

            int fila = 1;

            for (MorosoDto moroso : morosos) {

                Row row =
                        sheet.createRow(fila++);

                row.createCell(0)
                        .setCellValue(
                                moroso.getNumeroHermano());

                row.createCell(1)
                        .setCellValue(
                                moroso.getNombreCompleto());

                row.createCell(2)
                        .setCellValue(
                                moroso.getCuotasPendientes());

                row.createCell(3)
                        .setCellValue(
                                moroso.getImportePendiente()
                                        .doubleValue());
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            workbook.write(baos);

            return baos.toByteArray();
        }
    }

    public byte[] exportarCuotas()
            throws Exception {

        List<Cuota> cuotas =
                cuotaService.obtenerTodas();

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet("Cuotas");

            Row cabecera =
                    sheet.createRow(0);

            cabecera.createCell(0)
                    .setCellValue("Hermano");

            cabecera.createCell(1)
                    .setCellValue("Número");

            cabecera.createCell(2)
                    .setCellValue("Año");

            cabecera.createCell(3)
                    .setCellValue("Importe");

            cabecera.createCell(4)
                    .setCellValue("Estado");

            cabecera.createCell(5)
                    .setCellValue("Fecha Pago");

            int fila = 1;

            for (Cuota cuota : cuotas) {

                Row row =
                        sheet.createRow(fila++);

                row.createCell(0)
                        .setCellValue(
                                cuota.getHermano().getNombre()
                                        + " "
                                        + cuota.getHermano().getApellidos());

                row.createCell(1)
                        .setCellValue(
                                cuota.getHermano().getNumeroHermano());

                row.createCell(2)
                        .setCellValue(
                                cuota.getAnio());

                row.createCell(3)
                        .setCellValue(
                                cuota.getImporte().doubleValue());

                row.createCell(4)
                        .setCellValue(
                                cuota.getEstado().name());

                row.createCell(5)
                        .setCellValue(
                                cuota.getFechaPago() != null
                                        ? cuota.getFechaPago().toString()
                                        : "");
            }

            for (int i = 0; i < 6; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos =
                    new ByteArrayOutputStream();

            workbook.write(baos);

            return baos.toByteArray();
        }
    }
}