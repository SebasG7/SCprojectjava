package com.scprojectjava2.config;

import com.scprojectjava2.model.Role;
import com.scprojectjava2.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor para control de acceso basado en roles
 */
@Component
public class RoleAccessInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        // Si no hay usuario logueado, redirigir al login
        if (usuario == null) {
            response.sendRedirect("/login");
            return false;
        }        String requestURI = request.getRequestURI();
          // Rutas que solo pueden acceder los administradores
        String[] adminOnlyPaths = {
            "/usuarios",
            "/reportes-ventas"
        };
        
        // Rutas que pueden acceder tanto administradores como cajeros (con restricciones para cajeros)
        String[] sharedPaths = {
            "/productos",
            "/categorias",
            "/unidades",
            "/test-ventas",
            "/dashboard"
        };
        
        // Verificar si es una ruta solo para administradores
        for (String adminPath : adminOnlyPaths) {
            if (requestURI.startsWith(adminPath)) {
                if (!usuario.isAdministrador()) {
                    // Redirigir a una página de acceso denegado o al dashboard
                    response.sendRedirect("/dashboard?error=access_denied");
                    return false;
                }
                break;
            }
        }
          // Para rutas compartidas, verificar restricciones para cajeros
        for (String sharedPath : sharedPaths) {
            if (requestURI.startsWith(sharedPath) && usuario.isCajero()) {
                String method = request.getMethod();
                
                // Para productos, categorías y unidades: solo permitir GET (lectura) y bloquear exportaciones PDF
                if (requestURI.startsWith("/productos") || requestURI.startsWith("/categorias") || requestURI.startsWith("/unidades")) {
                    // Bloquear operaciones de escritura (POST, PUT, DELETE)
                    if (!"GET".equals(method)) {
                        response.sendRedirect("/dashboard?error=operation_not_allowed");
                        return false;
                    }
                    
                    // Bloquear exportaciones PDF específicamente
                    if (requestURI.contains("/reporte") || requestURI.contains("/pdf") || requestURI.contains("/export")) {
                        response.sendRedirect("/dashboard?error=operation_not_allowed");
                        return false;
                    }
                }
                break;
            }
        }
        
        return true;
    }
}
