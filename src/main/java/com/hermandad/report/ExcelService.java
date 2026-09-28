package com.hermandad.report;

import com.hermandad.dto.MorosoDto;
import com.hermandad.entity.Cuota;
import com.hermandad.entity.ElementoInventario;
import com.hermandad.entity.Socio;
import com.hermandad.service.CuotaService;
import com.hermandad.service.SocioService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ExcelService {

    private final SocioService socioService;

    private final CuotaService cuotaService;

    public ExcelService(
            SocioService socioService,
            CuotaService cuotaService) {

        this.socioService = socioService;
        this.cuotaService = cuotaService;
    }

    public byte[] exportarSocios()
            throws Exception {
        return exportarSocios(socioService.obtenerTodos());
    }

    public byte[] exportarSocios(List<Socio> socios)
            throws Exception {

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet(
                            "Socios");

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
            cabecera.createCell(5).setCellValue("Tipo");
            cabecera.createCell(6).setCellValue("Teléfono");
            cabecera.createCell(7).setCellValue("Email");
            cabecera.createCell(8).setCellValue("Dirección");
            cabecera.createCell(9).setCellValue("Fecha nacimiento");
            cabecera.createCell(10).setCellValue("Fecha alta");
            cabecera.createCell(11).setCellValue("Forma de pago");
            cabecera.createCell(12).setCellValue("IBAN");
            cabecera.createCell(13).setCellValue("Titular cuenta");

            int fila = 1;

            for (Socio socio : socios) {

                Row row =
                        sheet.createRow(fila++);

                row.createCell(0)
                        .setCellValue(
                                socio.getNumeroSocio() != null
                                        ? socio.getNumeroSocio()
                                        : 0);

                row.createCell(1)
                        .setCellValue(
                                socio.getNombre());

                row.createCell(2)
                        .setCellValue(
                                socio.getApellidos());

                row.createCell(3)
                        .setCellValue(
                                socio.getDni());

                row.createCell(4)
                        .setCellValue(
                                socio.getEstado() != null
                                        ? socio.getEstado().name()
                                        : "");
                row.createCell(5).setCellValue(socio.getTipo() != null ? socio.getTipo().name() : "");
                row.createCell(6).setCellValue(socio.getTelefono() != null ? socio.getTelefono() : "");
                row.createCell(7).setCellValue(socio.getEmail() != null ? socio.getEmail() : "");
                row.createCell(8).setCellValue(socio.getDireccion() != null ? socio.getDireccion() : "");
                row.createCell(9).setCellValue(socio.getFechaNacimiento() != null ? socio.getFechaNacimiento().toString() : "");
                row.createCell(10).setCellValue(socio.getFechaAlta() != null ? socio.getFechaAlta().toString() : "");
                row.createCell(11).setCellValue(socio.getFormaPago() != null ? socio.getFormaPago().name() : "");
                row.createCell(12).setCellValue(socio.getIban() != null ? socio.getIban() : "");
                row.createCell(13).setCellValue(socio.getTitularCuenta() != null ? socio.getTitularCuenta() : "");
            }

            for (int i = 0; i < 14; i++) {
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
                                moroso.getNumeroSocio());

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

    public byte[] exportarInventario(List<ElementoInventario> elementos) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Inventario");
            String[] cabeceras = {"Código", "Nombre", "Categoría", "Ubicación", "Estado", "Fecha de adquisición", "Valor de adquisición", "Activo", "Descripción", "Observaciones", "Imagen"};
            Row cabecera = sheet.createRow(0);
            for (int i = 0; i < cabeceras.length; i++) {
                cabecera.createCell(i).setCellValue(cabeceras[i]);
            }

            int fila = 1;
            for (ElementoInventario elemento : elementos) {
                Row row = sheet.createRow(fila++);
                row.createCell(0).setCellValue(elemento.getCodigo() != null ? elemento.getCodigo() : "");
                row.createCell(1).setCellValue(elemento.getNombre() != null ? elemento.getNombre() : "");
                row.createCell(2).setCellValue(elemento.getCategoria() != null ? elemento.getCategoria() : "");
                row.createCell(3).setCellValue(elemento.getUbicacion() != null ? elemento.getUbicacion() : "");
                row.createCell(4).setCellValue(elemento.getEstado() != null ? elemento.getEstado().name() : "");
                row.createCell(5).setCellValue(elemento.getFechaAdquisicion() != null ? elemento.getFechaAdquisicion().toString() : "");
                row.createCell(6).setCellValue(elemento.getValorAdquisicion() != null ? elemento.getValorAdquisicion().doubleValue() : 0);
                row.createCell(7).setCellValue(Boolean.TRUE.equals(elemento.getActivo()) ? "Sí" : "No");
                row.createCell(8).setCellValue(elemento.getDescripcion() != null ? elemento.getDescripcion() : "");
                row.createCell(9).setCellValue(elemento.getObservaciones() != null ? elemento.getObservaciones() : "");
                row.createCell(10).setCellValue(elemento.getImagenUrl() != null ? elemento.getImagenUrl() : "");
            }
            for (int i = 0; i < cabeceras.length; i++) sheet.autoSizeColumn(i);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    public byte[] exportarCuotas()
            throws Exception {
        return exportarCuotas(cuotaService.obtenerTodas());
    }

    public byte[] exportarCuotas(List<Cuota> cuotas)
            throws Exception {

        try (Workbook workbook =
                     new XSSFWorkbook()) {

            Sheet sheet =
                    workbook.createSheet("Cuotas");

            Row cabecera =
                    sheet.createRow(0);

            cabecera.createCell(0)
                    .setCellValue("Socio");

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
                                cuota.getSocio().getNombre()
                                        + " "
                                        + cuota.getSocio().getApellidos());

                row.createCell(1)
                        .setCellValue(
                                cuota.getSocio().getNumeroSocio());

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
