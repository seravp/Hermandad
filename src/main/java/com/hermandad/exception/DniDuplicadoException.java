package com.hermandad.exception;

public class DniDuplicadoException extends RuntimeException {

    public DniDuplicadoException(String dni) {
        super("Ya existe un hermano con el DNI " + dni);
    }

}
