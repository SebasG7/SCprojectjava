package com.scprojectjava2.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    
    @Autowired
    private RoleAccessInterceptor roleAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Interceptor de autenticación (primero)
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/logout", "/css/**", "/js/**", "/images/**", "/static/**");
        
        // Interceptor de control de acceso por roles (segundo)
        registry.addInterceptor(roleAccessInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/login", "/logout", "/css/**", "/js/**", "/images/**", "/static/**");
    }
}