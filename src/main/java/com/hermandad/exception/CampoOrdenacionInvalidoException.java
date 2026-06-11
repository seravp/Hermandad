package com.hermandad.exception;

public class CampoOrdenacionInvalidoException
        extends RuntimeException {

    public CampoOrdenacionInvalidoException(
            String mensaje) {

        super(mensaje);
    }
}
