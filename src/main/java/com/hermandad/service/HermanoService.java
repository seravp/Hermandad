package com.hermandad.service;

import com.hermandad.entity.Hermano;
import com.hermandad.repository.HermanoRepository;
import org.springframework.stereotype.Service;
import com.hermandad.exception.RecursoNoEncontradoException;

import java.util.List;

@Service
public class HermanoService {

    private final HermanoRepository hermanoRepository;

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

        return hermanoRepository.save(hermano);
    }

    public void eliminar(Long id) {

        if (!hermanoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("Hermano no encontrado");
        }

        hermanoRepository.deleteById(id);
    }

}