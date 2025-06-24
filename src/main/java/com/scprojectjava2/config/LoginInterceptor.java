package com.scprojectjava2.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        String requestURI = request.getRequestURI();        // Permitir acceso sin autenticación a estas rutas
        if (requestURI.equals("/") ||
            requestURI.equals("/login") || 
            requestURI.equals("/logout") || 
            requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") ||
            requestURI.startsWith("/static/")) {
            
            // Incluso para rutas públicas, agregar headers anti-cache si es logout
            if (requestURI.equals("/logout")) {
                response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
                response.setHeader("Pragma", "no-cache");
                response.setDateHeader("Expires", 0);
                response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"");
            }
            return true;
        }        // Agregar headers anti-cache para páginas protegidas (más agresivos)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Headers adicionales para prevenir navegación hacia atrás
        response.setHeader("Vary", "Cache-Control");
        response.setHeader("Last-Modified", "Thu, 01 Jan 1970 00:00:00 GMT");
        response.setHeader("ETag", "\"0\"");
        response.setHeader("Surrogate-Control", "no-store");
        
        HttpSession session = request.getSession(false);
        
        // Verificar si existe sesión y si tiene usuario válido
        if (session != null && session.getAttribute("usuario") != null) {
            // Verificar que la sesión no haya expirado
            try {
                session.getAttribute("usuario"); // Esto lanzará excepción si la sesión es inválida
                return true; // Usuario autenticado y sesión válida
            } catch (IllegalStateException e) {
                // Sesión inválida
                session = null;
            }
        }
        
        // Si llegamos aquí, no hay sesión válida
        // Limpiar cualquier sesión existente
        if (session != null) {
            try {
                session.invalidate();
            } catch (IllegalStateException e) {
                // Sesión ya inválida, ignorar
            }
        }
        
        // Redirigir al login
        response.sendRedirect("/login");
        return false;
    }
}
