package com.hermandad.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(
                    GlobalExceptionHandler.class);
    /**
     * Errores de validación de @Valid.
     * Se mantienen con el formato actual para facilitar
     * el tratamiento de errores campo a campo en Angular.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errores = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errores.put(
                                error.getField(),
                                error.getDefaultMessage()
                        ));

        return errores;
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleAuthentication(
            HttpServletRequest request) {

        return buildError(
                HttpStatus.UNAUTHORIZED,
                "Usuario o contraseña incorrectos.",
                request);
    }

    /**
     * DNI duplicado.
     */
    @ExceptionHandler(DniDuplicadoException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDniDuplicado(
            DniDuplicadoException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request);
    }

    /**
     * Recurso no encontrado.
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(
            RecursoNoEncontradoException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request);
    }

    /**
     * Campo de ordenación inválido.
     */
    @ExceptionHandler(CampoOrdenacionInvalidoException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleCampoOrdenacionInvalido(
            CampoOrdenacionInvalidoException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request);
    }

    /**
     * Cualquier otra excepción de negocio.
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.BAD_REQUEST,
                ex.getMessage(),
                request);
    }

    /**
     * Error inesperado.
     */

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiError handleAccessDenied(
            HttpServletRequest request) {

        return buildError(
                HttpStatus.FORBIDDEN,
                "No tienes permiso para realizar esta operación.",
                request);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<ApiError> handleResponseStatus(
            org.springframework.web.server.ResponseStatusException ex,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        return org.springframework.http.ResponseEntity.status(status)
                .body(buildError(status, ex.getReason(), request));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(
            Exception ex,
            HttpServletRequest request) {
        logger.error(
                "Error inesperado en {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);
        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Se ha producido un error interno.",
                request);
    }

    /**
     * Construye la respuesta estándar de error.
     */
    private ApiError buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        return new ApiError(
                status.value(),
                message,
                LocalDateTime.now(),
                request.getRequestURI()
        );
    }

}