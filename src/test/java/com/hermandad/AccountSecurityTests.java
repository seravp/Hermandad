package com.hermandad;

import com.hermandad.entity.*;
import com.hermandad.repository.UsuarioRepository;
import com.hermandad.security.*;
import com.hermandad.service.UsuarioService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class AccountSecurityTests {
    private static final String PASSWORD = "Una-clave-segura-2026";
    @Autowired WebApplicationContext context;
    @Autowired UsuarioRepository repository;
    @Autowired UsuarioService service;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    MockMvc mvc;
    Usuario admin;
    Usuario consulta;

    @BeforeEach void setup() {
        repository.deleteAll();
        admin = account("admin-test", Rol.ADMIN, true);
        consulta = account("consulta-test", Rol.CONSULTA, true);
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach void cleanup() { repository.deleteAll(); }

    private Usuario account(String name, Rol role, boolean active) {
        Usuario user = new Usuario();
        user.setUsername(name);
        user.setPassword(encoder.encode(PASSWORD));
        user.setRol(role);
        user.setActivo(active);
        return repository.saveAndFlush(user);
    }

    private String token(Usuario user) { return jwt.generateToken(new AuthenticatedUser(user)); }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.username").value(username)).andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(body, "$.token");
    }

    private void canRead(String token) throws Exception {
        mvc.perform(get("/api/socios").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    private void rejected(String token) throws Exception {
        mvc.perform(get("/api/socios").header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test void loginAuthenticatesAndReturnsTokenWithStableId() throws Exception {
        String token = login(consulta.getUsername(), PASSWORD);
        assertThat(jwt.validateToken(token).userId()).isEqualTo(consulta.getId());
        canRead(token);
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"username\":\"consulta-test\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test void anonymousAndConsultaCannotManageAccounts() throws Exception {
        mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
        String token = token(consulta);
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
        String body = "{\"username\":\"intruso\",\"password\":\"Una-clave-segura-2026\",\"rol\":\"ADMIN\",\"activo\":true}";
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + token)
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/" + consulta.getId()).header("Authorization", "Bearer " + token)
                .contentType("application/json").content(body)).andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/" + admin.getId() + "/password")
                .header("Authorization", "Bearer " + token).contentType("application/json")
                .content("{\"password\":\"Una-clave-segura-2026\"}")).andExpect(status().isForbidden());
        mvc.perform(delete("/api/usuarios/" + admin.getId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test void adminCanCreateAccountsWithoutExposingHash() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization", "Bearer " + token(admin))
                .contentType("application/json")
                .content("{\"username\":\"nuevo\",\"password\":\"Una-clave-segura-2026\",\"rol\":\"CONSULTA\",\"activo\":true}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        assertThat(encoder.matches(PASSWORD, repository.findByUsername("nuevo").orElseThrow().getPassword())).isTrue();
    }

    @Test void editingExistingUserPasswordFromFormPayloadChangesCredentialsAndVersion() throws Exception {
        Usuario prueba = account("prueba", Rol.CONSULTA, true);
        String previousToken = login("prueba", PASSWORD);
        long previousVersion = repository.findById(prueba.getId()).orElseThrow().getTokenVersion();
        // Same serialized payload asserted by password-flow-http.spec.ts after DOM input + click.
        String formPayload = """
                {"username":"prueba","rol":"CONSULTA","activo":true,"password":"Nueva-clave-prueba-2026"}
                """;
        mvc.perform(put("/api/usuarios/" + prueba.getId())
                .header("Authorization", "Bearer " + login(admin.getUsername(), PASSWORD))
                .contentType("application/json").content(formPayload))
                .andExpect(status().isOk()).andExpect(jsonPath("$.password").doesNotExist());
        Usuario persisted = repository.findById(prueba.getId()).orElseThrow();
        assertThat(persisted.getTokenVersion()).isEqualTo(previousVersion + 1);
        assertThat(encoder.matches("Nueva-clave-prueba-2026", persisted.getPassword())).isTrue();
        assertThat(encoder.matches(PASSWORD, persisted.getPassword())).isFalse();
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"username\":\"prueba\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isUnauthorized());
        canRead(login("prueba", "Nueva-clave-prueba-2026"));
        rejected(previousToken);
    }

    @Test void editingAccountAndPasswordIsAtomic() throws Exception {
        String oldToken = token(consulta);
        String url = "/api/usuarios/" + consulta.getId();
        mvc.perform(put(url).header("Authorization", "Bearer " + token(admin))
                .contentType("application/json")
                .content("{\"username\":\"renamed\",\"rol\":\"ADMIN\",\"activo\":true,\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
        Usuario unchanged = repository.findById(consulta.getId()).orElseThrow();
        assertThat(unchanged.getUsername()).isEqualTo("consulta-test");
        assertThat(unchanged.getRol()).isEqualTo(Rol.CONSULTA);
        canRead(oldToken);
        mvc.perform(put(url).header("Authorization", "Bearer " + token(admin))
                .contentType("application/json")
                .content("{\"username\":\"renamed\",\"rol\":\"CONSULTA\",\"activo\":true,\"password\":\"Otra-clave-segura-2026\"}"))
                .andExpect(status().isOk());
        rejected(oldToken);
        canRead(login("renamed", "Otra-clave-segura-2026"));
    }

    @Test void editingWithoutPasswordPreservesExistingPassword() throws Exception {
        mvc.perform(put("/api/usuarios/" + consulta.getId()).header("Authorization", "Bearer " + token(admin))
                .contentType("application/json")
                .content("{\"username\":\"renamed\",\"rol\":\"CONSULTA\",\"activo\":true}"))
                .andExpect(status().isOk());
        canRead(login("renamed", PASSWORD));
        assertThat(repository.findById(consulta.getId()).orElseThrow().getTokenVersion()).isZero();
    }

    @Test void passwordChangeRevokesOldTokenAndAllowsNewLogin() throws Exception {
        String oldToken = login(consulta.getUsername(), PASSWORD);
        mvc.perform(put("/api/usuarios/" + consulta.getId() + "/password")
                .header("Authorization", "Bearer " + token(admin)).contentType("application/json")
                .content("{\"password\":\"Otra-clave-segura-2026\"}")).andExpect(status().isOk());
        rejected(oldToken);
        canRead(login(consulta.getUsername(), "Otra-clave-segura-2026"));
    }

    @Test void disablingAndReenablingDoesNotReviveOldToken() throws Exception {
        String oldToken = token(consulta);
        consulta.setPassword(null); consulta.setActivo(false);
        service.actualizar(consulta.getId(), consulta);
        rejected(oldToken);
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"username\":\"consulta-test\",\"password\":\"Una-clave-segura-2026\"}"))
                .andExpect(status().isUnauthorized());
        consulta.setActivo(true);
        service.actualizar(consulta.getId(), consulta);
        rejected(oldToken);
        canRead(login(consulta.getUsername(), PASSWORD));
    }

    @Test void renamingAndReusingNameDoesNotTransferIdentityOrRoles() throws Exception {
        String oldToken = token(consulta);
        consulta.setPassword(null); consulta.setUsername("renamed");
        service.actualizar(consulta.getId(), consulta);
        account("consulta-test", Rol.ADMIN, true);
        canRead(oldToken);
        mvc.perform(get("/api/usuarios").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isForbidden());
    }

    @Test void deletingAndReusingNameDoesNotTransferToken() throws Exception {
        String oldToken = token(consulta);
        service.eliminar(consulta.getId());
        account("consulta-test", Rol.ADMIN, true);
        rejected(oldToken);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "short", "12345678901"})
    void invalidPasswordsRejectedForCreationAndReset(String password) {
        String previousHash = consulta.getPassword();
        assertThatThrownBy(() -> service.cambiarPassword(consulta.getId(), password))
                .isInstanceOf(ResponseStatusException.class);
        Usuario newUser = new Usuario();
        newUser.setUsername("invalid"); newUser.setRol(Rol.CONSULTA); newUser.setPassword(password);
        assertThatThrownBy(() -> service.guardar(newUser)).isInstanceOf(ResponseStatusException.class);
        Usuario unchanged = repository.findById(consulta.getId()).orElseThrow();
        assertThat(unchanged.getPassword()).isEqualTo(previousHash);
        assertThat(unchanged.getTokenVersion()).isZero();
    }

    @Test void passwordByteLimitRejectsMultibyteOverflow() {
        assertThatThrownBy(() -> service.cambiarPassword(consulta.getId(), "é".repeat(37)))
                .isInstanceOf(ResponseStatusException.class);
        service.cambiarPassword(consulta.getId(), "é".repeat(36));
        assertThat(encoder.matches("é".repeat(36), repository.findById(consulta.getId()).orElseThrow().getPassword())).isTrue();
    }

    @Test void invalidPasswordApiReturns400RatherThan500() throws Exception {
        mvc.perform(put("/api/usuarios/" + consulta.getId() + "/password")
                .header("Authorization", "Bearer " + token(admin)).contentType("application/json")
                .content("{\"password\":\"\"}")).andExpect(status().isBadRequest());
    }

    @Test void inactiveAdminDoesNotCountAsBackupForLastActiveAdmin() throws Exception {
        account("inactive-admin", Rol.ADMIN, false);
        Usuario updated = new Usuario();
        updated.setUsername(admin.getUsername()); updated.setRol(Rol.ADMIN); updated.setActivo(false);
        assertThatThrownBy(() -> service.actualizar(admin.getId(), updated)).isInstanceOf(ResponseStatusException.class);
        updated.setRol(Rol.CONSULTA); updated.setActivo(true);
        assertThatThrownBy(() -> service.actualizar(admin.getId(), updated)).isInstanceOf(ResponseStatusException.class);
        mvc.perform(delete("/api/usuarios/" + admin.getId()).header("Authorization", "Bearer " + token(admin)))
                .andExpect(status().isConflict());
        assertThat(repository.countByRolAndActivoTrue(Rol.ADMIN)).isEqualTo(1);
    }

    @Test void canRemoveAdminWhenAnotherActiveAdminExists() {
        account("backup-admin", Rol.ADMIN, true);
        service.eliminar(admin.getId());
        assertThat(repository.countByRolAndActivoTrue(Rol.ADMIN)).isEqualTo(1);
    }

    @Test void concurrentAdminDeactivationPreservesOneActiveAdmin() throws Exception {
        Usuario other = account("other-admin", Rol.ADMIN, true);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> deactivateAfter(start, admin));
            var second = executor.submit(() -> deactivateAfter(start, other));
            start.countDown();
            int successes = (first.get(15, TimeUnit.SECONDS) ? 1 : 0) + (second.get(15, TimeUnit.SECONDS) ? 1 : 0);
            assertThat(successes).isEqualTo(1);
            assertThat(repository.countByRolAndActivoTrue(Rol.ADMIN)).isEqualTo(1);
        }
    }

    private boolean deactivateAfter(CountDownLatch start, Usuario user) throws InterruptedException {
        start.await(); user.setPassword(null); user.setActivo(false);
        try { service.actualizar(user.getId(), user); return true; }
        catch (ResponseStatusException ex) { assertThat(ex.getStatusCode().value()).isEqualTo(409); return false; }
    }

    @Test void malformedExpiredLegacyAndWrongSignatureTokensReturn401() throws Exception {
        var key = Keys.hmacShaKeyFor("test-only-secret-key-with-at-least-32-characters".getBytes(StandardCharsets.UTF_8));
        rejected("invalid");
        rejected(Jwts.builder().subject(consulta.getId().toString()).claim("tokenVersion", 0)
                .expiration(new Date(System.currentTimeMillis() - 10000)).signWith(key).compact());
        rejected(Jwts.builder().subject(consulta.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 60000)).signWith(key).compact());
        rejected(Jwts.builder().subject(consulta.getId().toString()).claim("tokenVersion", 0)
                .signWith(key).compact());
        rejected(Jwts.builder().subject(consulta.getId().toString()).claim("tokenVersion", 0)
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(Jwts.SIG.HS256.key().build()).compact());
    }
}