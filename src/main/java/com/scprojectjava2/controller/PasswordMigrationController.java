package com.scprojectjava2.controller;

import com.scprojectjava2.service.PasswordMigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/migration")
public class PasswordMigrationController {

    @Autowired
    private PasswordMigrationService passwordMigrationService;

    /**
     * Obtiene estadísticas de migración
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMigrationStats() {
        try {
            PasswordMigrationService.MigrationStats stats = passwordMigrationService.getMigrationStats();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("totalUsers", stats.getTotalUsers());
            response.put("encryptedUsers", stats.getEncryptedUsers());
            response.put("plaintextUsers", stats.getPlaintextUsers());
            response.put("encryptionPercentage", stats.getEncryptionPercentage());
            response.put("message", stats.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error al obtener estadísticas: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * Ejecuta la migración de contraseñas
     */
    @PostMapping("/migrate")
    public ResponseEntity<Map<String, Object>> migratePasswords() {
        try {
            // Obtener estadísticas antes de la migración
            PasswordMigrationService.MigrationStats statsBefore = passwordMigrationService.getMigrationStats();
            
            // Ejecutar migración
            int migratedCount = passwordMigrationService.migratePasswordsToBeforeAddingEncryption();
            
            // Obtener estadísticas después de la migración
            PasswordMigrationService.MigrationStats statsAfter = passwordMigrationService.getMigrationStats();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("migratedCount", migratedCount);
            response.put("beforeMigration", Map.of(
                "totalUsers", statsBefore.getTotalUsers(),
                "encryptedUsers", statsBefore.getEncryptedUsers(),
                "plaintextUsers", statsBefore.getPlaintextUsers()
            ));
            response.put("afterMigration", Map.of(
                "totalUsers", statsAfter.getTotalUsers(),
                "encryptedUsers", statsAfter.getEncryptedUsers(),
                "plaintextUsers", statsAfter.getPlaintextUsers()
            ));
            response.put("message", String.format(
                "Migración completada exitosamente. %d contraseñas migradas a BCrypt.", 
                migratedCount
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Error durante la migración: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
