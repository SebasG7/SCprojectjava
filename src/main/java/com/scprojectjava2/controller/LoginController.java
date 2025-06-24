package com.scprojectjava2.controller;

import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class LoginController {
    
    @Autowired
    private UsuarioService usuarioService;
      @GetMapping("/login")
    public String mostrarLogin(HttpServletResponse response, HttpSession session) {
        // Invalidar cualquier sesión existente al mostrar login
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // Sesión ya inválida, ignorar
            }
        }
        
        // Headers para prevenir cache en página de login
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Vary", "Cache-Control");
        
        return "login";
    }
      @PostMapping("/login")
    public String procesarLogin(@RequestParam("nombreUsuario") String nombreUsuario,
                              @RequestParam("contrasena") String contrasena,
                              HttpSession session) {
        
        if (usuarioService.validarUsuario(nombreUsuario, contrasena)) {
            // Login exitoso
            Optional<Usuario> usuario = usuarioService.buscarPorNombreUsuario(nombreUsuario);
            if (usuario.isPresent()) {
                session.setAttribute("usuario", usuario.get());
                session.setAttribute("nombreUsuario", nombreUsuario);
                return "redirect:/dashboard"; // Redirige al dashboard después del login
            }
        }
          // Login fallido
        return "redirect:/login?error=1";
    }
      @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        // Invalidar la sesión completamente
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // Sesión ya inválida, ignorar
            }
        }
        
        // Headers agresivos para prevenir cache y navegación hacia atrás
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("Vary", "Cache-Control");
        response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"");
        
        return "redirect:/login?logout=1";
    }
}
