package com.hermandad.service;

import com.hermandad.entity.Usuario;
import com.hermandad.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(
            String username)
            throws UsernameNotFoundException {

        System.out.println("Buscando usuario: " + username);

        Usuario usuario =
                usuarioRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "Usuario no encontrado"));

        System.out.println("Usuario encontrado");
        System.out.println(usuario.getUsername());
        System.out.println(usuario.getPassword());
        System.out.println(usuario.getRol());

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().name())
                .build();
    }

}