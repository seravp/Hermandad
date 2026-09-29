# Flyway: adopción segura de Hermandad

## Qué cambia

Spring Boot 4.0.6 gestiona las versiones del starter Flyway, el módulo PostgreSQL y el driver JDBC. El plugin Maven usa esas mismas versiones y no está ligado al ciclo de build: `verify` no conecta a producción.

`V1__initial_schema.sql` crea las nueve tablas del modelo actual en una base VACÍA. No contiene datos, usuarios iniciales ni secretos. Se ha contrastado con `produccion_schema.sql` y reproduce su estructura, incluidas nulabilidades, nombres, orden de columnas, restricciones, índices y secuencias; véase `COMPARACION_ESQUEMA_FLYWAY.md`. La comprobación certifica ese volcado, no el estado en vivo ni sus datos. En una base existente se registra explícitamente un baseline de versión **1**, por lo que V1 queda excluida. No ejecutar V1 manualmente en producción.

`baseline-on-migrate=false`, `clean-disabled=true` y validación de migraciones permanecen activos. Una base no vacía sin historial se rechaza en vez de adoptarse automáticamente. El baseline solo registra el punto de partida; NO comprueba que las tablas sean correctas ni repara diferencias.

| Entorno | Flyway | Hibernate |
| --- | --- | --- |
| Local existente, perfil `local` | Desactivado para mantener compatibilidad | Configuración local existente (`update`) |
| Local adoptado, perfiles `local,flyway` | Activado | `validate` |
| `prod` y `docker` | Activado | `validate` |
| `test` | Activado, misma V1 | `validate`, H2 en memoria |

El archivo local ignorado por Git y las credenciales no se modifican. El perfil adicional `flyway` permite migrar local sin editar ese archivo. Para una base local existente, realizar antes el mismo proceso de revisión y baseline. Para una base vacía no hacer baseline: permitir que Flyway ejecute V1.

## Antes de adoptar producción

1. Preparar un backup completo con el procedimiento habitual y verificar su restauración en una base PostgreSQL aislada. Guardar también un export solo de estructura (`pg_dump --schema-only --no-owner --no-privileges`) con acceso restringido. No añadir dumps ni datos personales al repositorio.
2. Confirmar host, puerto, base, usuario, esquema efectivo y versión del servidor. Revisar `SELECT current_database(), current_user, current_schema(), version();` y `SHOW search_path;`. Los comandos siguientes suponen `public`; si no es el esquema real, detenerse y alinear Flyway y Hibernate expresamente. Revisar variables externas que puedan sobrescribir la configuración.
3. Comparar la estructura restaurada con V1: las nueve tablas, columnas y tipos, nulabilidad, claves, índices, restricciones, secuencias/identidades y valores siguientes. Pueden existir columnas/índices adicionales históricos que deben preservarse y documentarse. `ddl-auto=validate` no valida todos esos detalles. Verificar también recuentos y relaciones de datos antes/después.
4. Confirmar especialmente `socios.numero_socio`, `cuotas.socio_id`, ambos campos `tipo`, los dos importes de `configuracion` y las tablas de inventario/cuadrillas. El SQL histórico de `docs/migraciones/2026-09-28-renombrar-hermanos-a-socios.sql` NO se incorpora a Flyway ni se vuelve a ejecutar: presupone columnas antiguas y puede no corresponder al estado actual. Si producción no está al nivel de V1, resolver y ensayar esas diferencias antes de certificar el baseline; no falsear el historial.
5. Ejecutar el proceso de abajo primero contra la copia restaurada. Arrancar allí el backend con `validate`, comprobar funcionalidad y comparar datos. Verificar también una base vacía con V1. Ensayar con la misma versión principal de PostgreSQL que producción.
6. Solo después, planificar la ventana de adopción, detener las escrituras y ejecutar un único operador. No se ha realizado ningún despliegue ni conexión a producción con este cambio.

## Comandos de adopción (primero en la copia restaurada)

Desde la raíz del backend, aportar `FLYWAY_URL`, `FLYWAY_USER` y `FLYWAY_PASSWORD` mediante el gestor de secretos o el entorno del proceso, usando las credenciales existentes. La URL es JDBC. No poner contraseñas en comandos, POM, commits o documentación. El plugin Maven NO lee `spring.datasource.*` ni los perfiles de Spring.

Comprobar que `public.flyway_schema_history` no existe. Si ya existe, revisar su contenido e historial y detener este procedimiento de primer baseline: no borrarlo ni usar `repair` para ocultar errores.

```powershell
.\mvnw.cmd -B flyway:info '-Dflyway.defaultSchema=public' '-Dflyway.schemas=public'
```

Tras las comprobaciones estructurales y del destino, registrar explícitamente el baseline:

```powershell
.\mvnw.cmd -B flyway:baseline '-Dflyway.defaultSchema=public' '-Dflyway.schemas=public' '-Dflyway.baselineVersion=1'
.\mvnw.cmd -B flyway:info '-Dflyway.defaultSchema=public' '-Dflyway.schemas=public'
.\mvnw.cmd -B flyway:validate '-Dflyway.defaultSchema=public' '-Dflyway.schemas=public'
```

Revisar el código de salida de cada comando y detenerse ante cualquier fallo. Debe aparecer una fila BASELINE exitosa en versión 1. Con esta entrega no hay V2: V1 no debe ejecutarse en la base adoptada. El baseline crea únicamente el historial de Flyway, sin recrear las tablas de negocio. No establece ni modifica los valores de las secuencias existentes.

Después del ensayo y la autorización del despliegue, arrancar la versión preparada con el perfil de producción y `ddl-auto=validate`. Si falla la validación, detener el despliegue y analizar la discrepancia; nunca cambiar a `update`, activar baseline automático, borrar tablas o ejecutar `clean`. Si solo se ha registrado el baseline y no se han aplicado migraciones posteriores, la versión anterior puede seguir usando las tablas de negocio; conservar el historial para investigar. Para futuros cambios de datos/esquema, preparar un plan de recuperación específico y probado.

## Local y Docker

Para adoptar local, hacer backup/revisión/baseline de esa base y después arrancar con perfiles `local,flyway` (en ese orden). Para probar V1 usar una base vacía separada. No activar Flyway sobre el esquema existente sin haberlo revisado.

Docker monta actualmente `database/init`: si hay un dump allí, PostgreSQL lo ejecuta al inicializar un volumen nuevo y la base ya no estará vacía. Esa base debe revisarse y adoptar baseline antes de arrancar el backend. Para probar una base realmente vacía, usar un volumen independiente y un directorio de inicialización vacío. No borrar volúmenes existentes. La configuración Docker deja `ddl-auto=validate` fijo; la antigua variable `JPA_DDL_AUTO` ya no selecciona otro valor mediante ese archivo.

## Pruebas y cambios futuros

```powershell
.\mvnw.cmd -B verify
```

Las pruebas ejecutan V1 en H2 y verifican el arranque con Hibernate validate, inventario sin OSIV, rechazo de una base existente sin baseline, conservación de socios/cuotas después del baseline, identidades posteriores, segunda migración sin cambios y bloqueo de clean.

Para repetir en PostgreSQL, usar exclusivamente una base temporal dedicada y vacía. Definir `HERMANDAD_TEST_JDBC_URL`, `HERMANDAD_TEST_DB_USER` y `HERMANDAD_TEST_DB_PASSWORD` para `FlywaySafetyTests`; estas pruebas crean esquemas únicos y dejan sus datos para inspección. Para las pruebas Spring, pasar además `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` y `spring.datasource.driver-class-name=org.postgresql.Driver` mediante propiedades de sistema/entorno seguras. Eliminar únicamente el entorno desechable al finalizar. Nunca apuntar las pruebas a bases compartidas, local de trabajo o producción.

Los siguientes cambios se añaden como `V2__descripcion.sql`, `V3__descripcion.sql`, etc. No editar V1 una vez aplicada; no incluir datos reales o secretos. Probar la actualización de una copia existente y la creación desde cero. Cuando haya nuevas versiones, adaptar los tests que esperan una sola migración a la versión vigente.

Referencias: [starter de Spring Boot](https://docs.spring.io/spring-boot/4.0/reference/using/build-systems.html), [semántica de baseline](https://documentation.red-gate.com/flyway/reference/commands/baseline).
