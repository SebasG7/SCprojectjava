package com.scprojectjava2.config;

import com.scprojectjava2.model.Role;
import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos que se ejecuta al arrancar la aplicación.
 * Crea un usuario administrador de prueba si no existe ningún usuario en la base de datos.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private UsuarioService usuarioService;

    // Permitir configurar las credenciales del admin desde variables de entorno o application.properties
    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.name:Administrador del Sistema}")
    private String adminName;

    @Override
    public void run(String... args) throws Exception {
        logger.info("Iniciando verificación de datos iniciales...");
        
        try {
            // Verificar si ya existen usuarios en la base de datos
            long totalUsuarios = usuarioService.listarTodos().size();
            
            if (totalUsuarios == 0) {
                logger.info("Base de datos vacía. Creando usuario administrador de prueba...");
                crearUsuarioAdministrador();
            } else {
                logger.info("Ya existen {} usuario(s) en la base de datos. No se crearán usuarios de prueba.", totalUsuarios);
            }
            
        } catch (Exception e) {
            logger.error("Error durante la inicialización de datos: {}", e.getMessage(), e);
        }
    }

    private void crearUsuarioAdministrador() {
        try {
            // Verificar que no exista ya un usuario con el nombre de usuario especificado
            if (usuarioService.existeNombreUsuario(adminUsername)) {
                logger.warn("Ya existe un usuario con el nombre '{}'. No se creará duplicado.", adminUsername);
                return;
            }

            // Crear el usuario administrador
            Usuario admin = new Usuario(adminUsername, adminPassword, adminName, Role.ADMINISTRADOR);
            admin.setActivo(1);

            Usuario usuarioCreado = usuarioService.guardar(admin);
            
            logger.info("✓ Usuario administrador creado exitosamente:");
            logger.info("  - ID: {}", usuarioCreado.getId());
            logger.info("  - Usuario: {}", usuarioCreado.getNombreUsuario());
            logger.info("  - Nombre: {}", usuarioCreado.getNombre());
            logger.info("  - Role: {}", usuarioCreado.getRole());
            logger.info("  - Activo: {}", usuarioCreado.getActivo() == 1 ? "Sí" : "No");
            
            // También crear un usuario cajero de prueba
            crearUsuarioCajero();
            
        } catch (Exception e) {
            logger.error("Error al crear usuario administrador: {}", e.getMessage(), e);
        }
    }

    private void crearUsuarioCajero() {
        try {
            String cajeroUsername = "cajero";
            String cajeroPassword = "cajero123";
            String cajeroName = "Cajero 1";

            if (usuarioService.existeNombreUsuario(cajeroUsername)) {
                logger.warn("Ya existe un usuario con el nombre '{}'. No se creará duplicado.", cajeroUsername);
                return;
            }

            Usuario cajero = new Usuario(cajeroUsername, cajeroPassword, cajeroName, Role.CAJERO);
            cajero.setActivo(1);

            Usuario usuarioCreado = usuarioService.guardar(cajero);
            
            logger.info("✓ Usuario cajero creado exitosamente:");
            logger.info("  - ID: {}", usuarioCreado.getId());
            logger.info("  - Usuario: {}", usuarioCreado.getNombreUsuario());
            logger.info("  - Nombre: {}", usuarioCreado.getNombre());
            logger.info("  - Role: {}", usuarioCreado.getRole());
            
        } catch (Exception e) {
            logger.error("Error al crear usuario cajero: {}", e.getMessage(), e);
        }
    }
}
