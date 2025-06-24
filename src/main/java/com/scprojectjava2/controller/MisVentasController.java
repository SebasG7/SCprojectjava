package com.scprojectjava2.controller;

import com.scprojectjava2.model.*;
import com.scprojectjava2.repository.*;
import com.scprojectjava2.service.ReporteVentasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/mis-ventas")
public class MisVentasController {
    
    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    
    @Autowired
    private ReporteVentasService reporteVentasService;
    
    @GetMapping
    public String mostrarMisVentas(Model model, HttpSession session) {
        try {
            // Verificar usuario logueado
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            if (usuarioLogueado == null) {
                return "redirect:/login";
            }
            
            // Solo permitir acceso a cajeros y administradores
            if (usuarioLogueado.isCliente()) {
                return "redirect:/dashboard";
            }
            
            model.addAttribute("usuario", usuarioLogueado);
            
            // Obtener estadísticas del usuario
            String nombreUsuario = usuarioLogueado.getNombre();
            
            // Total de ventas del usuario
            Long totalVentas = ventaRepository.countByUsuarioCajero(nombreUsuario);
            Double totalIngresos = ventaRepository.getTotalIngresosByUsuarioCajero(nombreUsuario);
            
            // Ventas del mes actual
            LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
            List<Venta> ventasDelMes = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
                nombreUsuario, inicioMes, finMes);
            
            // Ventas del día actual
            LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
            LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);
            List<Venta> ventasDelDia = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
                nombreUsuario, inicioDia, finDia);
            
            // Últimas 10 ventas del usuario
            List<Venta> ultimasVentas = ventaRepository.findTop10ByUsuarioCajeroOrderByFechaDesc(nombreUsuario);
            
            model.addAttribute("totalVentas", totalVentas != null ? totalVentas : 0);
            model.addAttribute("totalIngresos", totalIngresos != null ? totalIngresos : 0.0);
            model.addAttribute("ventasDelMes", ventasDelMes.size());
            model.addAttribute("ingresosDelMes", ventasDelMes.stream().mapToDouble(Venta::getTotal).sum());
            model.addAttribute("ventasDelDia", ventasDelDia.size());
            model.addAttribute("ingresosDelDia", ventasDelDia.stream().mapToDouble(Venta::getTotal).sum());
            model.addAttribute("ultimasVentas", ultimasVentas);
            model.addAttribute("fechaActual", LocalDate.now());
            
            return "mis-ventas";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar mis ventas: " + e.getMessage());
            return "mis-ventas";
        }
    }
    
    // Filtrar mis ventas por fecha
    @GetMapping("/filtrar")
    @ResponseBody
    public List<Venta> filtrarMisVentas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "20") int limite,
            HttpSession session) {
        
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
        if (usuarioLogueado == null) {
            return new ArrayList<>();
        }
        
        String nombreUsuario = usuarioLogueado.getNombre();
        List<Venta> ventas;
        
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            ventas = ventaRepository.findVentasByUsuarioCajeroAndFechaBetweenWithDetalles(nombreUsuario, inicio, fin);
        } else {
            ventas = ventaRepository.findVentasByUsuarioCajeroWithDetalles(nombreUsuario);
        }
        
        // Limitar resultados
        return ventas.stream().limit(limite).toList();
    }
    
    // Obtener mis estadísticas
    @GetMapping("/estadisticas")
    @ResponseBody
    public Map<String, Object> getMisEstadisticas(HttpSession session) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            if (usuarioLogueado == null) {
                estadisticas.put("error", "Usuario no autenticado");
                return estadisticas;
            }
            
            String nombreUsuario = usuarioLogueado.getNombre();
            
            // Estadísticas generales
            Long totalVentas = ventaRepository.countByUsuarioCajero(nombreUsuario);
            Double totalIngresos = ventaRepository.getTotalIngresosByUsuarioCajero(nombreUsuario);
            
            estadisticas.put("totalVentas", totalVentas != null ? totalVentas : 0);
            estadisticas.put("totalIngresos", totalIngresos != null ? totalIngresos : 0.0);
            
            // Ventas del mes actual
            LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
            List<Venta> ventasDelMes = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
                nombreUsuario, inicioMes, finMes);
            estadisticas.put("ventasDelMes", ventasDelMes.size());
            estadisticas.put("ingresosDelMes", ventasDelMes.stream().mapToDouble(Venta::getTotal).sum());
            
            // Ventas del día actual
            LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
            LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);
            List<Venta> ventasDelDia = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
                nombreUsuario, inicioDia, finDia);
            estadisticas.put("ventasDelDia", ventasDelDia.size());
            estadisticas.put("ingresosDelDia", ventasDelDia.stream().mapToDouble(Venta::getTotal).sum());
            
        } catch (Exception e) {
            estadisticas.put("error", "Error al cargar estadísticas: " + e.getMessage());
        }
        
        return estadisticas;
    }
    
    // Exportar mis ventas a PDF
    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarMisVentasPDF(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpSession session) {
        
        try {
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            if (usuarioLogueado == null) {
                return ResponseEntity.badRequest().build();
            }
            
            String nombreUsuario = usuarioLogueado.getNombre();
            byte[] pdfBytes = reporteVentasService.generarReportePDFPorUsuario(nombreUsuario, fechaInicio, fechaFin);
            
            String filename = "mis_ventas_" + nombreUsuario.replaceAll("\\s+", "_") + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Exportar mis ventas a Excel
    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarMisVentasExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            HttpSession session) {
        
        try {
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            if (usuarioLogueado == null) {
                return ResponseEntity.badRequest().build();
            }
            
            String nombreUsuario = usuarioLogueado.getNombre();
            byte[] excelBytes = reporteVentasService.generarReporteExcelPorUsuario(nombreUsuario, fechaInicio, fechaFin);
            
            String filename = "mis_ventas_" + nombreUsuario.replaceAll("\\s+", "_") + "_" + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", filename);
            
            return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
