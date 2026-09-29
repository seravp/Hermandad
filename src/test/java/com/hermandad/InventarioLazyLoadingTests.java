package com.hermandad;

import com.hermandad.dto.RevisionInventarioDetalleRequestDto;
import com.hermandad.entity.*;
import com.hermandad.exception.BusinessException;
import com.hermandad.mapper.InventarioMapper;
import com.hermandad.repository.*;
import com.hermandad.service.InventarioService;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InventarioLazyLoadingTests {
    @Autowired InventarioService service;
    @Autowired InventarioMapper mapper;
    @Autowired ElementoInventarioRepository elementos;
    @Autowired RevisionInventarioRepository revisiones;
    @Autowired RevisionInventarioDetalleRepository detalles;
    @Autowired PlatformTransactionManager transactionManager;

    private Long revisionId;
    private Long detalleId;
    private String prefix;

    @BeforeEach
    void prepararDatosEnOtraTransaccion() {
        prefix = UUID.randomUUID().toString();
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            RevisionInventario revision = new RevisionInventario();
            revision.setTitulo("Prueba sin OSIV");
            revision.setFecha(LocalDateTime.now());
            revision.setUsuario("TEST");
            revision.setEstado(EstadoRevisionInventario.ABIERTA);
            revisionId = revisiones.save(revision).getId();
            for (String codigo : new String[]{"B", "A"}) {
                ElementoInventario elemento = new ElementoInventario();
                elemento.setCodigo(prefix + codigo);
                elemento.setNombre("Bien " + codigo);
                elemento.setCategoria("Enseres");
                elemento.setUbicacion("Almacen");
                elemento.setEstado(EstadoInventario.BUENO);
                elementos.save(elemento);
                RevisionInventarioDetalle detalle = new RevisionInventarioDetalle();
                detalle.setRevision(revision);
                detalle.setElemento(elemento);
                detalleId = detalles.save(detalle).getId();
            }
        });
    }

    @Test
    void listaOrdenadaSeMapeaDespuesDeCerrarLaTransaccion() {
        var resultado = service.obtenerDetalles(revisionId);
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        assertThat(resultado).hasSize(2);
        assertThat(resultado).allSatisfy(detalle ->
                assertThat(Hibernate.isInitialized(detalle.getElemento())).isTrue());
        assertThat(resultado.stream().map(mapper::toDetalleResponse).toList())
                .extracting(dto -> dto.getCodigo()).containsExactly(prefix + "A", prefix + "B");
    }

    @Test
    void actualizacionSeMapeaFueraDeLaTransaccionYSePersiste() {
        var dto = new RevisionInventarioDetalleRequestDto();
        dto.setVerificado(true);
        dto.setEstadoObservado(EstadoInventario.REGULAR);
        dto.setUbicacionObservada("  Sacristia  ");
        var resultado = service.actualizarDetalle(detalleId, dto);
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
        var respuesta = mapper.toDetalleResponse(resultado);
        assertThat(respuesta.getCodigo()).isEqualTo(prefix + "A");
        assertThat(respuesta.getVerificado()).isTrue();
        assertThat(respuesta.getFechaVerificacion()).isNotNull();
        assertThat(detalles.findById(detalleId).orElseThrow().getUbicacionObservada())
                .isEqualTo("Sacristia");
    }

    @Test
    void revisionCerradaSigueRechazandoModificaciones() {
        service.cerrarRevision(revisionId);
        var dto = new RevisionInventarioDetalleRequestDto();
        dto.setVerificado(true);
        assertThatThrownBy(() -> service.actualizarDetalle(detalleId, dto))
                .isInstanceOf(BusinessException.class).hasMessageContaining("cerrada");
        assertThat(detalles.findById(detalleId).orElseThrow().getVerificado()).isFalse();
    }

    @Test
    void revisionInexistenteMantieneElErrorDeNegocio() {
        assertThatThrownBy(() -> service.obtenerDetalles(-1L))
                .isInstanceOf(BusinessException.class);
    }
}
