package com.hermandad.security;

import com.hermandad.entity.Cuota;
import com.hermandad.entity.TipoSocio;
import com.hermandad.repository.CuotaRepository;
import com.hermandad.repository.SocioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component("cuotaPermissionService")
public class CuotaPermissionService {
    private final CuotaRepository cuotaRepository;
    private final SocioRepository socioRepository;

    public CuotaPermissionService(CuotaRepository cuotaRepository, SocioRepository socioRepository) {
        this.cuotaRepository = cuotaRepository;
        this.socioRepository = socioRepository;
    }

    public boolean puedeGestionarPorId(Long cuotaId) {
        return cuotaRepository.findById(cuotaId).map(Cuota::getTipo).map(this::puedeGestionarTipo).orElse(false);
    }

    public boolean puedeGestionarSocio(Long socioId) {
        return socioRepository.findById(socioId).map(socio -> socio.getTipo()).map(this::puedeGestionarTipo).orElse(false);
    }

    private boolean puedeGestionarTipo(TipoSocio tipo) {
        return tieneRol("ADMIN") || tieneRol("TESORERO") || (tipo == TipoSocio.COSTALERO && tieneRol("SECRETARIO"));
    }

    private boolean tieneRol(String rol) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_" + rol));
    }
}
