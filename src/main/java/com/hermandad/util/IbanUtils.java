package com.hermandad.util;

public final class IbanUtils {

    private IbanUtils() {
    }

    public static String normalizar(String iban) {

        if (iban == null || iban.isBlank()) {
            return null;
        }

        return iban.replace(" ", "")
                .toUpperCase();
    }

    public static boolean esValido(String iban) {

        if (iban == null || iban.isBlank()) {
            return true;
        }

        return normalizar(iban).matches("^ES\\d{22}$");
    }

}