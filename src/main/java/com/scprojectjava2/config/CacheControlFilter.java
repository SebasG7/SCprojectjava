package com.scprojectjava2.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CacheControlFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        
        // Aplicar headers de no-cache solo a páginas protegidas (no login, no recursos estáticos)
        if (!requestURI.contains("/login") && 
            !requestURI.contains("/static") && 
            !requestURI.contains("/css") && 
            !requestURI.contains("/js") && 
            !requestURI.contains("/images") &&
            !requestURI.contains("/favicon.ico")) {
            
            HttpSession session = httpRequest.getSession(false);
            
            // Si no hay sesión válida, redirigir al login
            if (session == null || session.getAttribute("usuario") == null) {
                if (!requestURI.equals("/logout")) {
                    httpResponse.sendRedirect("/login");
                    return;
                }
            }
            
            // Aplicar headers de no-cache para páginas protegidas
            httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, private");
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setDateHeader("Expires", 0);
            httpResponse.setHeader("X-Frame-Options", "DENY");
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        }
        
        chain.doFilter(request, response);
    }
}
