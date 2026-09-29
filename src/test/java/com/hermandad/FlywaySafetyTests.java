package com.hermandad;

import java.util.UUID;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import static org.assertj.core.api.Assertions.*;

class FlywaySafetyTests {
    private SingleConnectionDataSource connection;
    @AfterEach void closeConnection() { if (connection != null) connection.destroy(); }
    private DriverManagerDataSource database() throws java.sql.SQLException {
        // Optional dedicated disposable PostgreSQL database; never use application credentials.
        String url = System.getenv("HERMANDAD_TEST_JDBC_URL");
        if (url == null) url = "jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        var ds = new SingleConnectionDataSource(url,
                System.getenv().getOrDefault("HERMANDAD_TEST_DB_USER", "sa"),
                System.getenv().getOrDefault("HERMANDAD_TEST_DB_PASSWORD", ""), true);
        connection = ds;
        String schema = "test_" + UUID.randomUUID().toString().replace("-", "");
        new JdbcTemplate(ds).execute("CREATE SCHEMA " + schema);
        ds.setSchema(schema);
        ds.getConnection().setSchema(schema);
        return ds;
    }

    private Flyway flyway(DriverManagerDataSource ds) {
        return Flyway.configure().dataSource(ds).defaultSchema(ds.getSchema())
                .locations("classpath:db/migration").baselineVersion("1")
                .baselineOnMigrate(false).cleanDisabled(true)
                .validateMigrationNaming(true).load();
    }

    @Test
    void emptyDatabaseIsCreatedOnceAndCleanIsBlocked() throws Exception {
        var ds = database();
        var flyway = flyway(ds);
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(2);
        new JdbcTemplate(ds).update("INSERT INTO usuarios (username, activo) VALUES ('migration-test', true)");
        assertThat(flyway.migrate().migrationsExecuted).isZero();
        flyway.validate();
        assertThatThrownBy(flyway::clean).isInstanceOf(FlywayException.class);
        assertThat(new JdbcTemplate(ds).queryForObject("SELECT count(*) FROM usuarios", Long.class)).isEqualTo(1L);
    }

    @Test
    void existingDatabaseRequiresExplicitBaselineAndPreservesData() throws Exception {
        var ds = database();
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V1__initial_schema.sql")).execute(ds);
        var jdbc = new JdbcTemplate(ds);
        jdbc.update("INSERT INTO socios (nombre, apellidos, dni, tipo, numero_socio) VALUES ('Existing member', 'Test', '12345678A', 'HERMANO', 42)");
        jdbc.update("INSERT INTO cuotas (tipo, socio_id, importe) SELECT 'HERMANO', id, 50 FROM socios");
        var flyway = flyway(ds);
        assertThatThrownBy(flyway::migrate).isInstanceOf(FlywayException.class).hasMessageContaining("non-empty");
        jdbc.update("INSERT INTO usuarios (username, password, rol, activo) VALUES ('existing-admin', 'existing-hash', 'ADMIN', true)");
        flyway.baseline();
        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);
        flyway.validate();
        assertThat(jdbc.queryForObject("SELECT token_version FROM usuarios WHERE username='existing-admin'", Long.class)).isZero();
        assertThat(jdbc.queryForObject("SELECT password FROM usuarios WHERE username='existing-admin'", String.class)).isEqualTo("existing-hash");
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("2");
        assertThat(jdbc.queryForObject("SELECT count(*) FROM cuotas c JOIN socios s ON s.id=c.socio_id WHERE s.numero_socio=42 AND c.importe=50", Long.class)).isEqualTo(1L);
        jdbc.update("INSERT INTO socios (nombre, apellidos, dni, tipo) VALUES ('Next member', 'Test', '87654321B', 'HERMANO')");
        assertThat(jdbc.queryForObject("SELECT count(DISTINCT id) FROM socios", Long.class)).isEqualTo(2L);
    }

    @Test
    void productionRequiredMemberFieldsRejectNulls() throws Exception {
        var ds = database();
        flyway(ds).migrate();
        var jdbc = new JdbcTemplate(ds);
        String[] valid = {"Member", "Surname", "12345678A"};
        for (int required = 0; required < valid.length; required++) {
            Object[] values = valid.clone();
            values[required] = null;
            assertThatThrownBy(() -> jdbc.update(
                    "INSERT INTO socios (nombre, apellidos, dni, tipo) VALUES (?, ?, ?, 'HERMANO')", values))
                    .isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
        }
        assertThat(jdbc.queryForObject("SELECT count(*) FROM socios", Long.class)).isZero();
    }
}
