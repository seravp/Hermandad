package com.hermandad.security;

import com.hermandad.entity.Usuario;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;

/** Snapshot of the account that was actually authenticated. */
public class AuthenticatedUser extends User {
    private final Long id;
    private final long tokenVersion;

    public AuthenticatedUser(Usuario usuario) {
        super(usuario.getUsername(), usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getActivo()), true, true, true,
                AuthorityUtils.createAuthorityList("ROLE_" + usuario.getRol().name()));
        this.id = usuario.getId();
        this.tokenVersion = usuario.getTokenVersion();
    }

    public Long getId() { return id; }
    public long getTokenVersion() { return tokenVersion; }
}