package com.hermandad.exception;

public class DniDuplicadoException extends RuntimeException {

    public DniDuplicadoException(String dni) {
        super("Ya existe un socio con el DNI " + dni);
    }

}
