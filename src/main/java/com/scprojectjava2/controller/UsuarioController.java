package com.scprojectjava2.controller;

import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {    @Autowired
    private UsuarioService usuarioService;
    
    // Mostrar la página de gestión de usuarios
    @GetMapping
    public String mostrarUsuarios(@RequestParam(value = "accion", required = false) String accion,
                                 @RequestParam(value = "id", required = false) Integer id,
                                 Model model, HttpSession session) {
        
        // Obtener usuario de la sesión para verificación de roles en la vista
        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");
        if (usuarioSesion == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuarioSesion);
          try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            System.out.println("DEBUG: Usuarios encontrados: " + usuarios.size());
            for (Usuario u : usuarios) {
                System.out.println("DEBUG: Usuario - ID: " + u.getId() + 
                                 ", Nombre: " + u.getNombreUsuario() + 
                                 ", Activo: " + u.getActivo() + 
                                 ", Role: " + u.getRole() + 
                                 ", isAdmin: " + u.isAdministrador() + 
                                 ", isCajero: " + u.isCajero());
            }
            
            model.addAttribute("usuarios", usuarios);
            
            if (usuarios.isEmpty()) {
                model.addAttribute("infoMessage", "No hay usuarios registrados en el sistema.");
            }
            
            // Si es edición, cargar el usuario a editar
            if ("editar".equals(accion) && id != null) {
                Usuario usuarioEditar = usuarioService.obtenerUsuarioPorId(id);
                if (usuarioEditar != null) {
                    model.addAttribute("usuarioEditar", usuarioEditar);
                } else {
                    model.addAttribute("mensaje", "Usuario no encontrado");
                    model.addAttribute("tipo", "danger");
                }
            }
            
            return "usuarios";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("mensaje", "Error al cargar los usuarios: " + e.getMessage());
            model.addAttribute("tipo", "danger");
            return "usuarios";
        }
    }    // Guardar usuario (crear o actualizar)
    @PostMapping("/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario, 
                               @RequestParam(value = "role", required = false) String roleParam,
                               RedirectAttributes redirectAttributes) {
        try {            
            if (usuario.getId() != 0) {
                // Actualizar usuario existente
                Usuario usuarioExistente = usuarioService.obtenerUsuarioPorId(usuario.getId());
                if (usuarioExistente != null) {
                    usuarioExistente.setNombreUsuario(usuario.getNombreUsuario());
                    usuarioExistente.setNombre(usuario.getNombre());
                    usuarioExistente.setActivo(usuario.getActivo());
                    
                    // Actualizar rol
                    if (roleParam != null && !roleParam.trim().isEmpty()) {
                        try {
                            usuarioExistente.setRole(com.scprojectjava2.model.Role.valueOf(roleParam));
                        } catch (IllegalArgumentException e) {
                            usuarioExistente.setRole(com.scprojectjava2.model.Role.CAJERO); // Por defecto
                        }
                    }
                    
                    // Solo actualizar contraseña si se proporcionó una nueva
                    if (usuario.getContrasena() != null && !usuario.getContrasena().trim().isEmpty()) {
                        usuarioExistente.setContrasena(usuario.getContrasena());
                    }
                    // Si no se proporciona contraseña, mantener la existente
                    
                    usuarioService.guardarUsuario(usuarioExistente);
                    redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente");
                    redirectAttributes.addFlashAttribute("tipo", "success");
                } else {
                    redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
                    redirectAttributes.addFlashAttribute("tipo", "danger");
                }
            } else {
                // Crear nuevo usuario
                // Verificar si el nombre de usuario ya existe
                if (usuarioService.existeNombreUsuario(usuario.getNombreUsuario())) {
                    redirectAttributes.addFlashAttribute("mensaje", "El nombre de usuario ya existe");
                    redirectAttributes.addFlashAttribute("tipo", "warning");
                } else {
                    // Para usuarios nuevos, la contraseña es obligatoria
                    if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
                        redirectAttributes.addFlashAttribute("mensaje", "La contraseña es obligatoria para usuarios nuevos");
                        redirectAttributes.addFlashAttribute("tipo", "warning");
                        return "redirect:/usuarios";
                    }
                    
                    // Establecer rol
                    if (roleParam != null && !roleParam.trim().isEmpty()) {
                        try {
                            usuario.setRole(com.scprojectjava2.model.Role.valueOf(roleParam));
                        } catch (IllegalArgumentException e) {
                            usuario.setRole(com.scprojectjava2.model.Role.CAJERO); // Por defecto
                        }
                    } else {
                        usuario.setRole(com.scprojectjava2.model.Role.CAJERO); // Por defecto
                    }
                    
                    usuarioService.guardarUsuario(usuario);
                    redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente");
                    redirectAttributes.addFlashAttribute("tipo", "success");
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al guardar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        
        return "redirect:/usuarios";
    }

    // Obtener usuario por ID (para AJAX)
    @GetMapping("/obtener/{id}")
    @ResponseBody
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable int id) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario != null) {
                return ResponseEntity.ok(usuario);
            } else {
                return ResponseEntity.notFound().build();
            }        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Eliminar usuario
    @PostMapping("/eliminar")
    public String eliminarUsuario(@RequestParam int id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario != null) {
                // Verificar que no sea el último usuario activo
                List<Usuario> usuariosActivos = usuarioService.obtenerUsuariosActivos();
                if (usuariosActivos.size() == 1 && usuariosActivos.get(0).getId() == id) {
                    redirectAttributes.addFlashAttribute("mensaje", "No se puede eliminar el último usuario activo del sistema");
                    redirectAttributes.addFlashAttribute("tipo", "warning");
                } else {
                    usuarioService.eliminarUsuario(id);
                    redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente");
                    redirectAttributes.addFlashAttribute("tipo", "success");
                }
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
                redirectAttributes.addFlashAttribute("tipo", "danger");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al eliminar el usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        
        return "redirect:/usuarios";
    }

    // Cambiar estado de usuario (activar/desactivar)
    @PostMapping("/cambiar-estado/{id}")
    public String cambiarEstadoUsuario(@PathVariable int id, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuario = usuarioService.obtenerUsuarioPorId(id);
            if (usuario != null) {
                // Si se está desactivando, verificar que no sea el último usuario activo
                if (usuario.getActivo() == 1) {
                    List<Usuario> usuariosActivos = usuarioService.obtenerUsuariosActivos();
                    if (usuariosActivos.size() == 1) {
                        redirectAttributes.addFlashAttribute("mensaje", "No se puede desactivar el último usuario activo del sistema");
                        redirectAttributes.addFlashAttribute("tipo", "warning");
                        return "redirect:/usuarios";
                    }
                }
                
                usuario.setActivo(usuario.getActivo() == 1 ? 0 : 1);
                usuarioService.guardarUsuario(usuario);
                
                String estado = usuario.getActivo() == 1 ? "activado" : "desactivado";
                redirectAttributes.addFlashAttribute("mensaje", "Usuario " + estado + " correctamente");
                redirectAttributes.addFlashAttribute("tipo", "success");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Usuario no encontrado");
                redirectAttributes.addFlashAttribute("tipo", "danger");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensaje", "Error al cambiar el estado del usuario: " + e.getMessage());
            redirectAttributes.addFlashAttribute("tipo", "danger");
        }
        
        return "redirect:/usuarios";
    }

    // Generar reporte PDF de usuarios
    @GetMapping("/reporte")
    public ResponseEntity<byte[]> generarReportePDF() {
        try {
            List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
            
            // Crear contenido HTML simple para el PDF
            StringBuilder htmlContent = new StringBuilder();
            htmlContent.append("<!DOCTYPE html>");
            htmlContent.append("<html><head>");
            htmlContent.append("<meta charset='UTF-8'>");
            htmlContent.append("<title>Reporte de Usuarios</title>");
            htmlContent.append("<style>");
            htmlContent.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            htmlContent.append("h1 { color: #3a506b; text-align: center; }");
            htmlContent.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            htmlContent.append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            htmlContent.append("th { background-color: #3a506b; color: white; }");
            htmlContent.append("tr:nth-child(even) { background-color: #f2f2f2; }");
            htmlContent.append(".active { color: #28a745; font-weight: bold; }");
            htmlContent.append(".inactive { color: #dc3545; font-weight: bold; }");
            htmlContent.append("</style>");
            htmlContent.append("</head><body>");
            htmlContent.append("<h1>Reporte de Usuarios</h1>");
            htmlContent.append("<p>Fecha de generación: ").append(new java.util.Date()).append("</p>");
            htmlContent.append("<table>");
            htmlContent.append("<thead><tr>");
            htmlContent.append("<th>ID</th>");
            htmlContent.append("<th>Nombre de Usuario</th>");
            htmlContent.append("<th>Nombre Completo</th>");
            htmlContent.append("<th>Estado</th>");
            htmlContent.append("</tr></thead>");
            htmlContent.append("<tbody>");
            
            for (Usuario usuario : usuarios) {
                htmlContent.append("<tr>");
                htmlContent.append("<td>").append(usuario.getId()).append("</td>");
                htmlContent.append("<td>").append(usuario.getNombreUsuario()).append("</td>");
                htmlContent.append("<td>").append(usuario.getNombre()).append("</td>");
                htmlContent.append("<td class='").append(usuario.getActivo() == 1 ? "active" : "inactive").append("'>");
                htmlContent.append(usuario.getActivo() == 1 ? "Activo" : "Inactivo");
                htmlContent.append("</td>");
                htmlContent.append("</tr>");
            }
            
            htmlContent.append("</tbody></table>");
            htmlContent.append("</body></html>");
            
            // Por ahora, devolvemos el HTML como texto plano
            // En una implementación real, usarías una librería como iText o Flying Saucer para generar PDF
            byte[] pdfBytes = htmlContent.toString().getBytes("UTF-8");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "reporte_usuarios.html");
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
