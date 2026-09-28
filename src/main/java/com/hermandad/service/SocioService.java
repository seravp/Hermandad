package com.hermandad.service;

import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.FormaPago;
import com.hermandad.entity.Socio;
import com.hermandad.entity.TipoSocio;
import com.hermandad.exception.BusinessException;
import com.hermandad.exception.CampoOrdenacionInvalidoException;
import com.hermandad.exception.DniDuplicadoException;
import com.hermandad.repository.SocioRepository;
import com.hermandad.repository.ConfiguracionCuadrillaRepository;
import com.hermandad.exception.RecursoNoEncontradoException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.hermandad.dto.ImportacionSociosResponseDto;
import com.hermandad.util.IbanUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;


@Service
public class SocioService {

    private final SocioRepository socioRepository;
    private final ConfiguracionCuadrillaRepository configuracionCuadrillaRepository;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "numeroSocio",
            "nombre",
            "apellidos",
            "dni",
            "fechaAlta",
            "estado"
    );

    private final AuditoriaService auditoriaService;

    public SocioService(
            SocioRepository socioRepository,
            ConfiguracionCuadrillaRepository configuracionCuadrillaRepository,
            AuditoriaService auditoriaService) {

        this.socioRepository = socioRepository;
        this.configuracionCuadrillaRepository = configuracionCuadrillaRepository;
        this.auditoriaService = auditoriaService;
    }

    public List<Socio> obtenerTodos() {
        return socioRepository.findAll();
    }

    public Socio guardar(Socio socio) {

        validarTipo(socio.getTipo());

        if (socioRepository.existsByDni(socio.getDni())) {
            throw new DniDuplicadoException(socio.getDni());
        }

        Integer ultimoNumero =
                socioRepository.obtenerUltimoNumeroSocio();

        socio.setNumeroSocio((ultimoNumero != null ? ultimoNumero : 0) + 1);

        socio.setFechaAlta(LocalDate.now());

        socio.setFechaCreacion(LocalDateTime.now());

        socio.setFechaModificacion(LocalDateTime.now());

        Socio guardado =
                socioRepository.save(socio);

        auditoriaService.registrar(
                "CREAR",
                "SOCIO",
                guardado.getId());

        return guardado;
    }

    public Socio obtenerPorId(Long id) {

        return socioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Socio no encontrado"));
    }

    public Socio actualizar(Long id, Socio datos) {

        Socio socio = socioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Socio no encontrado"));

        socio.setNombre(datos.getNombre());
        socio.setApellidos(datos.getApellidos());

        // Si finalmente decides permitir editar el DNI

        if (!socio.getDni().equals(datos.getDni())
                && socioRepository.existsByDni(datos.getDni())) {

            throw new DniDuplicadoException(datos.getDni());

        }

        String iban = IbanUtils.normalizar(datos.getIban());

        if (!IbanUtils.esValido(iban)) {
            throw new BusinessException("El formato del IBAN no es válido.");
        }

        socio.setDni(datos.getDni());

        socio.setTelefono(datos.getTelefono());
        socio.setEmail(datos.getEmail());
        socio.setDireccion(datos.getDireccion());

        socio.setFechaNacimiento(datos.getFechaNacimiento());

        socio.setEstado(datos.getEstado());
        validarTipo(datos.getTipo());
        socio.setTipo(datos.getTipo());
        socio.setCuadrilla(datos.getCuadrilla());
        socio.setFormaPago(datos.getFormaPago());

        if (datos.getFormaPago() == FormaPago.EFECTIVO) {
            socio.setIban(null);
            socio.setTitularCuenta(null);
        } else {
            socio.setIban(iban);
            socio.setTitularCuenta(datos.getTitularCuenta());
        }

        socio.setFechaModificacion(LocalDateTime.now());

        Socio actualizado = socioRepository.save(socio);



        auditoriaService.registrar(
                "MODIFICAR",
                "SOCIO",
                actualizado.getId());

        return actualizado;
    }

    public Socio asignarPosicionCuadrilla(Long id, Integer posicion) {
        Socio socio = obtenerPorId(id);
        if (socio.getCuadrilla() == null || socio.getCuadrilla().isBlank()) {
            throw new BusinessException("El socio debe tener una cuadrilla asignada.");
        }
        int capacidad = configuracionCuadrillaRepository.findByNombre(socio.getCuadrilla())
                .map(configuracion -> configuracion.getFilas() * configuracion.getColumnas())
                .orElse(24);
        if (posicion == null || posicion < 1 || posicion > capacidad) {
            throw new BusinessException("La posición debe estar entre 1 y " + capacidad + ".");
        }

        Integer posicionAnterior = socio.getPosicionCuadrilla();
        Socio ocupante = socioRepository
                .findByCuadrillaAndPosicionCuadrilla(socio.getCuadrilla(), posicion)
                .orElse(null);

        socio.setPosicionCuadrilla(posicion);
        if (ocupante != null && !ocupante.getId().equals(socio.getId())) {
            ocupante.setPosicionCuadrilla(posicionAnterior);
            ocupante.setFechaModificacion(LocalDateTime.now());
            socioRepository.save(ocupante);
        }

        socio.setFechaModificacion(LocalDateTime.now());
        Socio actualizado = socioRepository.save(socio);
        auditoriaService.registrar("ASIGNAR_POSICION", "SOCIO", actualizado.getId());
        return actualizado;
    }

    public Socio liberarPosicionCuadrilla(Long id) {
        Socio socio = obtenerPorId(id);
        socio.setPosicionCuadrilla(null);
        socio.setFechaModificacion(LocalDateTime.now());
        Socio actualizado = socioRepository.save(socio);
        auditoriaService.registrar("LIBERAR_POSICION", "SOCIO", actualizado.getId());
        return actualizado;
    }

    public void eliminar(Long id) {

        Socio socio = socioRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException("Socio no encontrado"));

        auditoriaService.registrar(
                "ELIMINAR",
                "SOCIO",
                socio.getId());

        socioRepository.delete(socio);
    }

    public Socio buscarPorDni(String dni) {

        return socioRepository.findByDni(dni)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Socio no encontrado"));
    }

    public List<Socio> buscarPorEstado(
            EstadoSocio estado) {

        return socioRepository.findByEstado(estado);
    }

    public List<Socio> buscarPorApellidos(String apellidos) {

        return socioRepository
                .findByApellidosContainingIgnoreCase(apellidos);
    }

    public Page<Socio> obtenerPaginados(
            int page,
            int size,
            String sort,
            String direction) {

        if (!CAMPOS_ORDENABLES.contains(sort)) {
            throw new CampoOrdenacionInvalidoException(
                    "Campo de ordenación no permitido: " + sort);
        }

        Sort.Direction direccion =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direccion, sort)
                );

        return socioRepository.findAll(pageable);
    }

    public Page<Socio> buscarPaginado(
            String texto,
            EstadoSocio estado,
            TipoSocio tipo,
            String cuadrilla,
            int page,
            int size,
            String sort,
            String direction) {

        if (texto == null) {
            texto = "";
        }

        if (!CAMPOS_ORDENABLES.contains(sort)) {
            throw new CampoOrdenacionInvalidoException(
                    "Campo de ordenación no permitido: " + sort);
        }

        Sort.Direction direccion =
                direction.equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Pageable pageable =
                PageRequest.of(
                        page,
                        size,
                        Sort.by(direccion, sort));

        return socioRepository.buscar(
                texto,
                estado,
                tipo,
                cuadrilla,
                pageable);
    }


    public List<Socio> obtenerDomiciliados() {

        return socioRepository.findByFormaPago(
                FormaPago.DOMICILIACION);
    }

    public ImportacionSociosResponseDto importarExcel(java.io.InputStream contenido) {
        int importados = 0;
        int omitidos = 0;
        List<String> errores = new java.util.ArrayList<>();

        try (Workbook libro = WorkbookFactory.create(contenido)) {
            Sheet hoja = libro.getNumberOfSheets() > 0 ? libro.getSheetAt(0) : null;
            if (hoja == null || hoja.getPhysicalNumberOfRows() == 0) {
                throw new BusinessException("El archivo Excel no contiene datos.");
            }

            Map<String, Integer> columnas = columnas(hoja.getRow(hoja.getFirstRowNum()));
            validarCabeceras(columnas);
            DataFormatter formato = new DataFormatter(Locale.forLanguageTag("es-ES"));

            for (int indice = hoja.getFirstRowNum() + 1; indice <= hoja.getLastRowNum(); indice++) {
                Row fila = hoja.getRow(indice);
                if (fila == null || filaVacia(fila, formato)) {
                    continue;
                }
                try {
                    Socio socio = socioDesdeFila(fila, columnas, formato);
                    guardar(socio);
                    importados++;
                } catch (RuntimeException error) {
                    omitidos++;
                    errores.add("Fila " + (indice + 1) + ": " + error.getMessage());
                }
            }
        } catch (BusinessException error) {
            throw error;
        } catch (Exception error) {
            throw new BusinessException("No se ha podido leer el archivo Excel. Comprueba que es un archivo válido.");
        }

        return new ImportacionSociosResponseDto(importados, omitidos, errores);
    }

    private Map<String, Integer> columnas(Row cabecera) {
        if (cabecera == null) {
            throw new BusinessException("El Excel debe incluir una fila de cabeceras.");
        }
        DataFormatter formato = new DataFormatter(Locale.forLanguageTag("es-ES"));
        Map<String, Integer> columnas = new HashMap<>();
        for (Cell celda : cabecera) {
            String nombre = normalizarCabecera(formato.formatCellValue(celda));
            if (!nombre.isBlank()) columnas.put(nombre, celda.getColumnIndex());
        }
        return columnas;
    }

    private void validarCabeceras(Map<String, Integer> columnas) {
        for (String obligatoria : List.of("nombre", "apellidos", "dni", "tipo")) {
            if (!columnas.containsKey(obligatoria)) {
                throw new BusinessException("Falta la columna obligatoria: " + obligatoria + ".");
            }
        }
    }

    private Socio socioDesdeFila(Row fila, Map<String, Integer> columnas, DataFormatter formato) {
        Socio socio = new Socio();
        socio.setNombre(valor(fila, columnas, formato, "nombre", true));
        socio.setApellidos(valor(fila, columnas, formato, "apellidos", true));
        socio.setDni(valor(fila, columnas, formato, "dni", true).toUpperCase(Locale.ROOT));
        socio.setTelefono(valor(fila, columnas, formato, "telefono", false));
        socio.setEmail(valor(fila, columnas, formato, "email", false));
        socio.setDireccion(valor(fila, columnas, formato, "direccion", false));
        socio.setFechaNacimiento(fecha(fila, columnas.get("fecha nacimiento"), formato));
        socio.setEstado(enumValor(EstadoSocio.class, valor(fila, columnas, formato, "estado", false), EstadoSocio.ACTIVO, "estado"));
        socio.setTipo(enumValor(TipoSocio.class, valor(fila, columnas, formato, "tipo", true), null, "tipo"));
        socio.setCuadrilla(valor(fila, columnas, formato, "cuadrilla", false));
        socio.setFormaPago(enumValor(FormaPago.class, valor(fila, columnas, formato, "forma de pago", false), FormaPago.EFECTIVO, "forma de pago"));
        socio.setIban(valor(fila, columnas, formato, "iban", false));
        socio.setTitularCuenta(valor(fila, columnas, formato, "titular cuenta", false));
        return socio;
    }

    private String valor(Row fila, Map<String, Integer> columnas, DataFormatter formato, String cabecera, boolean obligatorio) {
        Integer columna = columnas.get(cabecera);
        String valor = columna == null || fila.getCell(columna) == null ? "" : formato.formatCellValue(fila.getCell(columna)).trim();
        if (obligatorio && valor.isBlank()) {
            throw new BusinessException("El campo " + cabecera + " es obligatorio.");
        }
        return valor;
    }

    private LocalDate fecha(Row fila, Integer columna, DataFormatter formato) {
        if (columna == null || fila.getCell(columna) == null || formato.formatCellValue(fila.getCell(columna)).isBlank()) return null;
        Cell celda = fila.getCell(columna);
        if (DateUtil.isCellDateFormatted(celda)) {
            return celda.getDateCellValue().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        String texto = formato.formatCellValue(celda).trim();
        for (DateTimeFormatter patron : List.of(DateTimeFormatter.ISO_LOCAL_DATE, DateTimeFormatter.ofPattern("d/M/uuuu"), DateTimeFormatter.ofPattern("d-M-uuuu"))) {
            try { return LocalDate.parse(texto, patron); } catch (DateTimeParseException ignored) { }
        }
        throw new BusinessException("La fecha de nacimiento no tiene un formato válido.");
    }

    private <T extends Enum<T>> T enumValor(Class<T> clase, String texto, T defecto, String campo) {
        if (texto == null || texto.isBlank()) return defecto;
        try {
            return Enum.valueOf(clase, normalizarEnum(texto));
        } catch (IllegalArgumentException error) {
            throw new BusinessException("El valor de " + campo + " no es válido: " + texto + ".");
        }
    }

    private boolean filaVacia(Row fila, DataFormatter formato) {
        for (Cell celda : fila) if (!formato.formatCellValue(celda).trim().isBlank()) return false;
        return true;
    }

    private String normalizarCabecera(String valor) {
        return java.text.Normalizer.normalize(valor, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toLowerCase(Locale.ROOT);
    }

    private String normalizarEnum(String valor) {
        return normalizarCabecera(valor).replace(' ', '_').toUpperCase(Locale.ROOT);
    }

    private void validarTipo(TipoSocio tipo) {
        if (tipo == null) {
            throw new BusinessException("Debe seleccionarse si el socio es hermano o costalero.");
        }
    }
}
