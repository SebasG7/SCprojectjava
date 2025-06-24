package com.scprojectjava2.controller;

import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.model.Producto;
import com.scprojectjava2.repository.*;
import com.scprojectjava2.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class IndexController {
    
    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
      @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private UnidadRepository unidadRepository;
    
    @Autowired
    private ProductoService productoService;
      @GetMapping("/")
    public String mostrarLanding() {
        // Siempre mostrar la landing page en la ruta raíz
        return "landing";
    }    @GetMapping("/dashboard")
    public String mostrarDashboard(HttpSession session, Model model) {
        // Obtener el usuario de la sesión
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        // Verificar autenticación
        if (nombreUsuario == null || usuario == null) {
            return "redirect:/login";
        }
        
        // Pasar el objeto usuario completo al modelo para acceso a roles
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreUsuario", nombreUsuario);        // Estadísticas iniciales para el dashboard
        try {
            Long totalProductos = (long) productoRepository.findByActivo(1).size();
            Long totalCategorias = categoriaRepository.countByActivoTrue();
            Long totalVentas = ventaRepository.count();
              // Calcular total de unidades de medida activas en el sistema (no unidades vendidas)
            Long totalUnidades = (long) unidadRepository.findAllActivos().size();
              // Obtener productos próximos a vencer (dentro de 5 días)
            List<Producto> productosProximosAVencer = productoService.obtenerProductosProximosAVencer();            // Obtener productos con stock bajo (menos de 10 unidades)
            List<Producto> productosConStockBajo = productoService.obtenerProductosConStockBajo();
            
            // Obtener productos vencidos
            List<Producto> productosVencidos = productoService.obtenerProductosVencidos();
            
            // Obtener productos agotados
            List<Producto> productosAgotados = productoService.obtenerProductosAgotados();
            
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("totalCategorias", totalCategorias);
            model.addAttribute("totalVentas", totalVentas);
            model.addAttribute("totalUnidades", totalUnidades);
            model.addAttribute("productosProximosAVencer", productosProximosAVencer);
            model.addAttribute("productosConStockBajo", productosConStockBajo);
            model.addAttribute("productosVencidos", productosVencidos);
            model.addAttribute("productosAgotados", productosAgotados);        } catch (Exception e) {
            // En caso de error, usar valores por defecto
            model.addAttribute("totalProductos", 0L);
            model.addAttribute("totalCategorias", 0L);
            model.addAttribute("totalVentas", 0L);            model.addAttribute("totalUnidades", 0L);
            model.addAttribute("productosProximosAVencer", new java.util.ArrayList<>());
            model.addAttribute("productosConStockBajo", new java.util.ArrayList<>());
            model.addAttribute("productosVencidos", new java.util.ArrayList<>());
            model.addAttribute("productosAgotados", new java.util.ArrayList<>());
        }
        
        return "dashboard";
    }
      @GetMapping("/migration")
    public String mostrarMigracion(HttpSession session, Model model) {
        // Verificar autenticación - solo usuarios autenticados pueden acceder
        String nombreUsuario = (String) session.getAttribute("nombreUsuario");
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (nombreUsuario == null || usuario == null) {
            return "redirect:/login";
        }
        
        // Pasar el objeto usuario completo al modelo
        model.addAttribute("usuario", usuario);
        model.addAttribute("nombreUsuario", nombreUsuario);
        
        return "migration";
    }
    
    @GetMapping("/test-modal")
    public String testModal() {
        return "test-modal";
    }
}
