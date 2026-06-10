package com.hermandad.controller;

import com.hermandad.entity.Hermano;
import com.hermandad.service.HermanoService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/hermanos")
public class HermanoController {

    private final HermanoService hermanoService;

    public HermanoController(HermanoService hermanoService) {
        this.hermanoService = hermanoService;
    }

    @GetMapping
    public List<Hermano> obtenerTodos() {
        return hermanoService.obtenerTodos();
    }

    @PostMapping
    public Hermano crear(@Valid @RequestBody Hermano hermano) {
        return hermanoService.guardar(hermano);
    }

    @GetMapping("/{id}")
    public Hermano obtenerPorId(@PathVariable Long id) {
        return hermanoService.obtenerPorId(id);
    }

    @PutMapping("/{id}")
    public Hermano actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Hermano hermano) {

        return hermanoService.actualizar(id, hermano);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Long id) {
        hermanoService.eliminar(id);
    }


}