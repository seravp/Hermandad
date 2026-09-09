package com.hermandad.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerarHash {

    public static void main(String[] args) {

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        System.out.println(
                encoder.encode("admin123"));
    }
}