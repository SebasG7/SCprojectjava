// Script de seguridad para prevenir navegación hacia atrás después del logout
// Este script debe incluirse en todas las páginas protegidas

(function() {
    'use strict';
    
    console.log('Script de seguridad cargado');
    
    // Función para configurar headers anti-cache desde el lado del cliente
    function configurarAntiCache() {
        // Agregar meta tags dinámicamente para anti-cache
        const metaTags = [
            { 'http-equiv': 'Cache-Control', content: 'no-cache, no-store, must-revalidate, max-age=0, private' },
            { 'http-equiv': 'Pragma', content: 'no-cache' },
            { 'http-equiv': 'Expires', content: '0' }
        ];
        
        metaTags.forEach(function(tag) {
            const meta = document.createElement('meta');
            Object.keys(tag).forEach(function(key) {
                meta.setAttribute(key, tag[key]);
            });
            document.head.appendChild(meta);
        });
    }
    
    // Función para prevenir navegación hacia atrás en páginas protegidas
    function configurarPrevencionNavegacion() {
        console.log('Configurando prevención de navegación para página protegida');
        
        // Verificar si venimos de logout
        const urlParams = new URLSearchParams(window.location.search);
        const isLogoutRedirect = urlParams.has('logout') || 
                                document.referrer.includes('/logout') ||
                                sessionStorage.getItem('just_logged_out') === 'true';
        
        if (isLogoutRedirect) {
            console.log('Detectado redirect desde logout, limpiando historial');
            
            // Limpiar el historial
            if (window.history.replaceState) {
                window.history.replaceState(null, null, window.location.pathname);
            }
            
            // Marcar que acabamos de hacer logout
            sessionStorage.setItem('just_logged_out', 'true');
        }
        
        // Agregar entradas al historial para prevenir navegación hacia atrás
        for (let i = 0; i < 3; i++) {
            history.pushState(null, null, window.location.href);
        }
        
        // Manejar evento popstate (navegación hacia atrás)
        window.addEventListener('popstate', function(event) {
            console.log('Intento de navegación hacia atrás detectado');
            
            // Verificar si la sesión sigue válida
            verificarSesionValida().then(function(sesionValida) {
                if (!sesionValida) {
                    console.log('Sesión inválida, redirigiendo a login');
                    window.location.replace('/login?session_expired=1');
                } else {
                    // Sesión válida, permitir navegación pero mantener protección
                    history.pushState(null, null, window.location.href);
                }
            }).catch(function() {
                // Error verificando sesión, asumir que es inválida
                window.location.replace('/login?session_expired=1');
            });
        });
    }
    
    // Función para verificar si la sesión sigue siendo válida
    function verificarSesionValida() {
        return new Promise(function(resolve) {
            fetch('/dashboard', {
                method: 'HEAD',
                cache: 'no-cache',
                headers: {
                    'Cache-Control': 'no-cache'
                }
            }).then(function(response) {
                // Si obtenemos 200, la sesión es válida
                // Si obtenemos 302 (redirect), la sesión probablemente expiró
                resolve(response.ok);
            }).catch(function() {
                // Error de red o sesión inválida
                resolve(false);
            });
        });
    }
    
    // Función para manejar el evento beforeunload
    function configurarBeforeUnload() {
        window.addEventListener('beforeunload', function(e) {
            // Limpiar marcadores de estado
            sessionStorage.removeItem('just_logged_out');
            
            // Agregar timestamp para detectar recargas
            sessionStorage.setItem('page_unload_time', Date.now().toString());
        });
    }
    
    // Función para manejar la carga de página
    function configurarPageLoad() {
        window.addEventListener('load', function() {
            const unloadTime = sessionStorage.getItem('page_unload_time');
            const currentTime = Date.now();
            
            // Si la página se cargó muy rápido después de unload, podría ser cache
            if (unloadTime && (currentTime - parseInt(unloadTime)) < 100) {
                console.log('Posible carga desde cache detectada');
                
                // Verificar sesión y recargar si es necesario
                verificarSesionValida().then(function(sesionValida) {
                    if (!sesionValida) {
                        window.location.replace('/login?cache_detected=1');
                    }
                });
            }
            
            sessionStorage.removeItem('page_unload_time');
        });
        
        // Manejar pageshow para cache del navegador
        window.addEventListener('pageshow', function(event) {
            if (event.persisted) {
                console.log('Página cargada desde cache del navegador');
                
                // Verificar sesión inmediatamente
                verificarSesionValida().then(function(sesionValida) {
                    if (!sesionValida) {
                        window.location.replace('/login?bfcache_detected=1');
                    } else {
                        // Reconfigurar prevención de navegación
                        configurarPrevencionNavegacion();
                    }
                });
            }
        });
    }
    
    // Función para bloquear teclas de navegación
    function configurarBloqueoTeclas() {
        document.addEventListener('keydown', function(e) {
            // Bloquear Alt + Flecha Izquierda (navegación hacia atrás)
            if (e.altKey && e.keyCode === 37) {
                e.preventDefault();
                console.log('Tecla de navegación hacia atrás bloqueada');
                return false;
            }
            
            // Bloquear F5 (recarga) si la sesión no es válida
            if (e.keyCode === 116) {
                verificarSesionValida().then(function(sesionValida) {
                    if (!sesionValida) {
                        e.preventDefault();
                        window.location.replace('/login?session_expired=1');
                        return false;
                    }
                });
            }
        });
    }
    
    // Inicialización cuando el DOM esté listo
    function inicializar() {
        configurarAntiCache();
        configurarPrevencionNavegacion();
        configurarBeforeUnload();
        configurarPageLoad();
        configurarBloqueoTeclas();
        
        console.log('Sistema de seguridad para páginas protegidas inicializado');
    }
    
    // Ejecutar cuando el DOM esté listo
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', inicializar);
    } else {
        inicializar();
    }
})();
