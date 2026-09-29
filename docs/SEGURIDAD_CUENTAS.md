# Cambios de seguridad de cuentas

- Los JWT nuevos identifican al usuario por ID y versión de acceso, no por nombre reutilizable.
- Cambiar la contraseña o cambiar el estado activo incrementa la versión y revoca los JWT anteriores. Reactivar una cuenta no restaura sus tokens antiguos.
- Crear o cambiar contraseñas exige al menos 12 caracteres Unicode, contenido no vacío y un máximo de 72 bytes UTF-8 (límite de BCrypt). Las contraseñas existentes siguen siendo válidas.
- El formulario guarda los datos y la contraseña opcional en una sola transacción. Una contraseña inválida no deja cambios parciales.
- Solo los administradores activos cuentan como respaldo del último administrador. Las modificaciones se serializan mediante bloqueos de base de datos para proteger cambios concurrentes.
- La validación del JWT se ejecuta una vez; los tokens inválidos no autentican la petición.

## Despliegue

Estos cambios son locales. La migración Flyway V2 añade `usuarios.token_version` con valor inicial 0, sin alterar contraseñas ni cuentas. Debe aplicarse sobre una base que ya tenga la V1 registrada o la línea base aprobada. No modificar V1 ni activar baselines automáticos.

Al desplegar, todos los usuarios deberán iniciar sesión nuevamente: los JWT del formato anterior se rechazan deliberadamente. Desplegar backend y frontend coordinadamente. Mantener copia de seguridad previa y verificar Flyway y el login tras el despliegue.

## Validación

Las pruebas de AccountSecurityTests usan el perfil test con H2 y peticiones MockMvc: permisos ADMIN/CONSULTA, firma/caducidad, revocación, nombres reutilizados, validación de contraseña, transacciones y último administrador con cambios concurrentes. FlywaySafetyTests comprueba que V2 conserva datos existentes. No sustituyen una prueba de despliegue sobre PostgreSQL.

La limitación de intentos de login en el servidor/proxy sigue pendiente de revisión; no se ha añadido ni cambiado en esta intervención.