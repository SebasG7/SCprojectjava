package com.scprojectjava2.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Clase temporal para generar hash BCrypt para la contraseña del administrador
 */
public class GenerarHashBCrypt {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Generar hash para la contraseña del administrador
        String plainPassword = "admin123";
        String hashedPassword = encoder.encode(plainPassword);
        
        System.out.println("=== GENERADOR DE HASH BCRYPT ===");
        System.out.println("Contraseña original: " + plainPassword);
        System.out.println("Hash BCrypt generado:");
        System.out.println(hashedPassword);
        System.out.println("");
        System.out.println("SQL para actualizar data.sql:");
        System.out.println("INSERT INTO usuarios (nombre_usuario, contrasena, nombre, activo)");
        System.out.println("VALUES ('admin', '" + hashedPassword + "', 'Administrador', 1)");
        System.out.println("ON DUPLICATE KEY UPDATE nombre_usuario = nombre_usuario;");
        System.out.println("================================");
        
        // Verificar que el hash funciona
        boolean matches = encoder.matches(plainPassword, hashedPassword);
        System.out.println("Verificación del hash: " + (matches ? "✅ CORRECTO" : "❌ ERROR"));
    }
}
