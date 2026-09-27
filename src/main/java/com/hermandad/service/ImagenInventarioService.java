package com.hermandad.service;

import com.hermandad.exception.BusinessException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class ImagenInventarioService {
    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> TIPOS_PERMITIDOS = Set.of("image/jpeg", "image/png", "image/webp");
    private final Path directorio = Path.of("uploads", "inventario").toAbsolutePath().normalize();

    public String guardar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) throw new BusinessException("Debes seleccionar una imagen.");
        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) throw new BusinessException("Solo se permiten imágenes JPG, PNG o WEBP.");
        if (archivo.getSize() > MAX_BYTES) throw new BusinessException("La imagen no puede superar 5 MB.");
        String extension = archivo.getContentType().equals("image/png") ? ".png" : archivo.getContentType().equals("image/webp") ? ".webp" : ".jpg";
        String nombre = UUID.randomUUID() + extension;
        try { Files.createDirectories(directorio); Files.copy(archivo.getInputStream(), directorio.resolve(nombre), StandardCopyOption.REPLACE_EXISTING); return nombre; }
        catch (IOException e) { throw new BusinessException("No se ha podido guardar la imagen."); }
    }

    public Resource cargar(String nombre) {
        try { Path archivo = directorio.resolve(nombre).normalize(); if (!archivo.startsWith(directorio)) throw new BusinessException("La imagen solicitada no es válida."); Resource recurso = new UrlResource(archivo.toUri()); if (recurso.exists() && recurso.isReadable()) return recurso; }
        catch (Exception ignored) { }
        throw new BusinessException("La imagen solicitada no existe.");
    }

    public String tipoContenido(String nombre) {
        String extension = nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
        return switch (extension) {
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
