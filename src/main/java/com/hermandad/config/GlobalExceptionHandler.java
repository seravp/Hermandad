package com.hermandad.config;

import com.hermandad.exception.ApiError;
import com.hermandad.exception.CampoOrdenacionInvalidoException;
import com.hermandad.exception.DniDuplicadoException;
import com.hermandad.exception.RecursoNoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request) {

        return buildError(
                HttpStatus.CONFLICT,
                ex.getMessage(),
                request);
    }

    /**
     * Error inesperado.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleException(
            Exception ex,
            HttpServletRequest request) {

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