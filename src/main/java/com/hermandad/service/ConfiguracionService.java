package com.hermandad.service;

import com.hermandad.dto.ConfiguracionDto;
import com.hermandad.entity.Configuracion;
import com.hermandad.repository.ConfiguracionRepository;
import com.hermandad.repository.ConfiguracionCuadrillaRepository;
import com.hermandad.repository.SocioRepository;
import com.hermandad.dto.ConfiguracionCuadrillaDto;
import com.hermandad.entity.ConfiguracionCuadrilla;
import com.hermandad.util.IbanUtils;
import org.springframework.stereotype.Service;
import com.hermandad.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Year;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;
    private final ConfiguracionCuadrillaRepository configuracionCuadrillaRepository;
    private final SocioRepository socioRepository;
    private static final List<String> NOMBRES_CUADRILLAS = List.of("Nuestra Señora de los Dolores", "Nuestro Padre Jesús Nazareno", "Santo Entierro de Cristo", "Calvario", "Nazarenos", "Otros");

    public ConfiguracionService(ConfiguracionRepository configuracionRepository,
                                ConfiguracionCuadrillaRepository configuracionCuadrillaRepository,
                                SocioRepository socioRepository) {
        this.configuracionRepository = configuracionRepository;
        this.configuracionCuadrillaRepository = configuracionCuadrillaRepository;
        this.socioRepository = socioRepository;
    }

    public Configuracion obtenerConfiguracion() {
        return configuracionRepository.findById(1L)
                .orElseGet(this::crearConfiguracionInicial);
    }

    private Configuracion crearConfiguracionInicial() {

        Configuracion configuracion = new Configuracion();

        configuracion.setId(1L);
        configuracion.setNombreHermandad("Mi Hermandad");
        configuracion.setImporteCuotaHermano(BigDecimal.valueOf(50));
        configuracion.setImporteCuotaCostalero(BigDecimal.valueOf(50));
        configuracion.setAnioActivo(Year.now().getValue());

        return configuracionRepository.save(configuracion);
    }

    public Configuracion actualizar(ConfiguracionDto dto) {

        Configuracion configuracion = obtenerConfiguracion();

        if (dto.getImporteCuotaHermano() == null
                || dto.getImporteCuotaHermano().compareTo(BigDecimal.ZERO) <= 0
                || dto.getImporteCuotaCostalero() == null
                || dto.getImporteCuotaCostalero().compareTo(BigDecimal.ZERO) <= 0) {

            throw new BusinessException(
                    "Los importes de las cuotas deben ser mayores que cero.");
        }

        if (dto.getAnioActivo() == null
                || dto.getAnioActivo() < 2000 || dto.getAnioActivo() > 2100) {

            throw new BusinessException(
                    "El año activo no es válido.");
        }

        if (dto.getNombreHermandad() == null
                || dto.getNombreHermandad().isBlank()) {

            throw new BusinessException(
                    "El nombre de la hermandad es obligatorio.");
        }

        if (dto.getTelefono() != null
                && !dto.getTelefono().isBlank()
                && !dto.getTelefono().matches("^\\d{9}$")) {

            throw new BusinessException(
                    "El teléfono no es válido.");
        }

        if (dto.getEmail() != null
                && !dto.getEmail().isBlank()
                && !dto.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new BusinessException(
                    "El email no es válido.");
        }

        configuracion.setNombreHermandad(dto.getNombreHermandad());
        configuracion.setImporteCuotaHermano(dto.getImporteCuotaHermano());
        configuracion.setImporteCuotaCostalero(dto.getImporteCuotaCostalero());
        configuracion.setAnioActivo(dto.getAnioActivo());
        configuracion.setCif(dto.getCif());
        configuracion.setDireccion(dto.getDireccion());
        configuracion.setTelefono(dto.getTelefono());
        configuracion.setEmail(dto.getEmail());
        String iban = IbanUtils.normalizar(dto.getIban());



        if (!IbanUtils.esValido(iban)) {
            throw new BusinessException("El formato del IBAN no es válido.");
        }

        configuracion.setIban(iban);

        Configuracion actualizada = configuracionRepository.save(configuracion);
        if (dto.getCuadrillas() != null) {
            actualizarCuadrillas(dto.getCuadrillas());
        }
        return actualizada;
    }

    public List<ConfiguracionCuadrillaDto> obtenerCuadrillas() {
        asegurarCuadrillasIniciales();
        return configuracionCuadrillaRepository.findAllByOrderByNombreAsc().stream()
                .map(this::aDto)
                .toList();
    }

    public ConfiguracionCuadrillaDto obtenerCuadrilla(String nombre) {
        asegurarCuadrillasIniciales();
        return configuracionCuadrillaRepository.findByNombre(nombre)
                .map(this::aDto)
                .orElseThrow(() -> new BusinessException("La cuadrilla indicada no existe."));
    }

    public void actualizarCuadrillas(List<ConfiguracionCuadrillaDto> datos) {
        Map<String, ConfiguracionCuadrillaDto> porNombre = datos.stream()
                .filter(dato -> dato.getNombre() != null)
                .collect(Collectors.toMap(ConfiguracionCuadrillaDto::getNombre, Function.identity(), (primero, segundo) -> primero));
        if (!porNombre.keySet().equals(Set.copyOf(NOMBRES_CUADRILLAS))) {
            throw new BusinessException("Debes configurar todas las cuadrillas disponibles.");
        }
        for (String nombre : NOMBRES_CUADRILLAS) {
            ConfiguracionCuadrillaDto dato = porNombre.get(nombre);
            if (dato.getFilas() == null || dato.getColumnas() == null || dato.getFilas() < 1 || dato.getFilas() > 20 || dato.getColumnas() < 1 || dato.getColumnas() > 8) {
                throw new BusinessException("Las filas deben estar entre 1 y 20 y las columnas entre 1 y 8.");
            }
            int capacidad = dato.getFilas() * dato.getColumnas();
            if (socioRepository.existsByCuadrillaAndPosicionCuadrillaGreaterThan(nombre, capacidad)) {
                throw new BusinessException("No se puede reducir el croquis de " + nombre + " porque contiene posiciones asignadas fuera del nuevo límite.");
            }
            ConfiguracionCuadrilla configuracion = configuracionCuadrillaRepository.findByNombre(nombre).orElseGet(ConfiguracionCuadrilla::new);
            configuracion.setNombre(nombre);
            configuracion.setFilas(dato.getFilas());
            configuracion.setColumnas(dato.getColumnas());
            configuracionCuadrillaRepository.save(configuracion);
        }
    }

    private void asegurarCuadrillasIniciales() {
        for (String nombre : NOMBRES_CUADRILLAS) {
            if (configuracionCuadrillaRepository.findByNombre(nombre).isEmpty()) {
                ConfiguracionCuadrilla configuracion = new ConfiguracionCuadrilla();
                configuracion.setNombre(nombre);
                configuracion.setFilas(8);
                configuracion.setColumnas(3);
                configuracionCuadrillaRepository.save(configuracion);
            }
        }
    }

    private ConfiguracionCuadrillaDto aDto(ConfiguracionCuadrilla configuracion) {
        ConfiguracionCuadrillaDto dto = new ConfiguracionCuadrillaDto();
        dto.setNombre(configuracion.getNombre());
        dto.setFilas(configuracion.getFilas());
        dto.setColumnas(configuracion.getColumnas());
        return dto;
    }

}
