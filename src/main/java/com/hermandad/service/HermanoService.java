package com.hermandad.service;

import com.hermandad.entity.EstadoHermano;
import com.hermandad.entity.Hermano;
import com.hermandad.exception.CampoOrdenacionInvalidoException;
import com.hermandad.repository.HermanoRepository;
import com.hermandad.exception.RecursoNoEncontradoException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;


@Service
public class HermanoService {

    private final HermanoRepository hermanoRepository;

    private static final Set<String> CAMPOS_ORDENABLES = Set.of(
            "numeroHermano",
            "nombre",
            "apellidos",
            "dni",
            "fechaAlta",
            "estado"
    );

    public HermanoService(HermanoRepository hermanoRepository) {
        this.hermanoRepository = hermanoRepository;
    }

    public List<Hermano> obtenerTodos() {
        return hermanoRepository.findAll();
    }

    public Hermano guardar(Hermano hermano) {

        if (hermanoRepository.existsByDni(hermano.getDni())) {
            throw new RuntimeException("Ya existe un hermano con ese DNI");
        }

        Integer ultimoNumero =
                hermanoRepository.obtenerUltimoNumeroHermano();

        hermano.setNumeroHermano(ultimoNumero + 1);

        hermano.setFechaAlta(LocalDate.now());

        hermano.setFechaCreacion(LocalDateTime.now());

        hermano.setFechaModificacion(LocalDateTime.now());

        return hermanoRepository.save(hermano);
    }

    public Hermano obtenerPorId(Long id) {

        return hermanoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Hermano no encontrado"));
    }

    public Hermano actualizar(Long id, Hermano datos) {

        Hermano hermano = hermanoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Hermano no encontrado"));

        hermano.setNombre(datos.getNombre());
        hermano.setApellidos(datos.getApellidos());
        hermano.setTelefono(datos.getTelefono());
        hermano.setEmail(datos.getEmail());
        hermano.setDireccion(datos.getDireccion());
        hermano.setFechaNacimiento(datos.getFechaNacimiento());
        hermano.setEstado(datos.getEstado());
        hermano.setFechaModificacion(LocalDateTime.now());

        return hermanoRepository.save(hermano);
    }

    public void eliminar(Long id) {

        if (!hermanoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Hermano no encontrado");
        }

        hermanoRepository.deleteById(id);
    }

    public Hermano buscarPorDni(String dni) {

        return hermanoRepository.findByDni(dni)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Hermano no encontrado"));
    }

    public List<Hermano> buscarPorEstado(
            EstadoHermano estado) {

        return hermanoRepository.findByEstado(estado);
    }

    public List<Hermano> buscarPorApellidos(String apellidos) {

        return hermanoRepository
                .findByApellidosContainingIgnoreCase(apellidos);
    }

    public Page<Hermano> obtenerPaginados(
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

        return hermanoRepository.findAll(pageable);
    }

    public Page<Hermano> buscarPaginado(
            String nombre,
            String apellidos,
            String dni,
            EstadoHermano estado,
            int page,
            int size,
            String sort,
            String direction) {

        if (nombre == null) {
            nombre = "";
        }

        if (apellidos == null) {
            apellidos = "";
        }

        if (dni == null) {
            dni = "";
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

        return hermanoRepository.buscar(
                nombre,
                apellidos,
                dni,
                estado,
                pageable);
    }
}