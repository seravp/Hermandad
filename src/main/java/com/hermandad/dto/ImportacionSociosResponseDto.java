package com.hermandad.dto;

import java.util.List;

public record ImportacionSociosResponseDto(
        int importados,
        int omitidos,
        List<String> errores) {
}
