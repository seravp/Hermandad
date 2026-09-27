package com.hermandad.service;

import com.hermandad.dto.ConfiguracionDto;
import com.hermandad.entity.Configuracion;
import com.hermandad.repository.ConfiguracionRepository;
import com.hermandad.util.IbanUtils;
import org.springframework.stereotype.Service;
import com.hermandad.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Year;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
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

        return configuracionRepository.save(configuracion);
    }

}
