package com.hermandad.service;

import com.hermandad.entity.EstadoSocio;
import com.hermandad.entity.FormaPago;
import com.hermandad.entity.Socio;
import com.hermandad.entity.TipoSocio;
import com.hermandad.exception.BusinessException;
import com.hermandad.exception.CampoOrdenacionInvalidoException;
import com.hermandad.exception.DniDuplicadoException;
import com.hermandad.repository.SocioRepository;
import com.hermandad.exception.RecursoNoEncontradoException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import com.hermandad.util.IbanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;


@Service
public class SocioService {

    private final SocioRepository socioRepository;

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
            AuditoriaService auditoriaService) {

        this.socioRepository = socioRepository;
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

        socio.setNumeroSocio(ultimoNumero + 1);

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
                pageable);
    }


    public List<Socio> obtenerDomiciliados() {

        return socioRepository.findByFormaPago(
                FormaPago.DOMICILIACION);
    }

    private void validarTipo(TipoSocio tipo) {
        if (tipo == null) {
            throw new BusinessException("Debe seleccionarse si el socio es hermano o costalero.");
        }
    }
}
