package com.scprojectjava2.controller;

import com.scprojectjava2.model.*;
import com.scprojectjava2.repository.*;
import com.scprojectjava2.service.ReporteVentasService;
import com.scprojectjava2.service.EmailService;
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
@RequestMapping("/reportes-ventas")
public class ReporteVentasController {
    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    
    @Autowired
    private ProductoRepository productoRepository;    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private UnidadRepository unidadRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private ReporteVentasService reporteVentasService;
    
    @Autowired
    private EmailService emailService;

    @GetMapping
    public String mostrarReportes(Model model, HttpSession session) {
        try {
            // Verificar usuario logueado y su rol
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuario");
            if (usuarioLogueado == null) {
                return "redirect:/login";
            }
            model.addAttribute("usuario", usuarioLogueado);
            
            // Estadísticas generales
            long totalVentas = ventaRepository.count();
            double totalIngresos = ventaRepository.findAll().stream()
                .mapToDouble(Venta::getTotal)
                .sum();
            
            // Últimas 10 ventas
            List<Venta> ultimasVentas = ventaRepository.findTop10ByOrderByFechaDesc();
            
            // Productos más vendidos (top 5)
            List<Object[]> productosMasVendidos = detalleVentaRepository.getProductosMasVendidos();
            List<Map<String, Object>> topProductos = new ArrayList<>();
            for (int i = 0; i < Math.min(5, productosMasVendidos.size()); i++) {
                Object[] row = productosMasVendidos.get(i);
                Map<String, Object> producto = new HashMap<>();
                producto.put("id", row[0]);
                producto.put("nombre", row[1]);
                producto.put("totalVendido", row[2]);
                topProductos.add(producto);
            }
            
            // Ventas del mes actual
            LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
            List<Venta> ventasDelMes = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicioMes, finMes);
            double ingresosDelMes = ventasDelMes.stream().mapToDouble(Venta::getTotal).sum();
            
            // Lista de usuarios activos para el filtro (solo para administradores)
            List<Usuario> usuariosActivos = new ArrayList<>();
            if (usuarioLogueado.isAdministrador()) {
                usuariosActivos = usuarioRepository.findByActivo(1);
            }
            
            model.addAttribute("totalVentas", totalVentas);
            model.addAttribute("totalIngresos", totalIngresos);
            model.addAttribute("ultimasVentas", ultimasVentas);
            model.addAttribute("topProductos", topProductos);
            model.addAttribute("ventasDelMes", ventasDelMes.size());
            model.addAttribute("ingresosDelMes", ingresosDelMes);
            model.addAttribute("fechaActual", LocalDate.now());
            model.addAttribute("usuariosActivos", usuariosActivos);
            
            return "reportes-ventas";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los reportes: " + e.getMessage());
            return "reportes-ventas";
        }
    }

    @GetMapping("/productos-mas-vendidos")
    @ResponseBody
    public List<Map<String, Object>> getProductosMasVendidos(@RequestParam(defaultValue = "10") int limite) {
        List<Object[]> resultados = detalleVentaRepository.getProductosMasVendidos();
        List<Map<String, Object>> productos = new ArrayList<>();
        
        for (int i = 0; i < Math.min(limite, resultados.size()); i++) {
            Object[] row = resultados.get(i);
            Map<String, Object> producto = new HashMap<>();
            producto.put("id", row[0]);
            producto.put("nombre", row[1]);
            producto.put("totalVendido", row[2]);
            productos.add(producto);
        }
        
        return productos;
    }

    @GetMapping("/ventas-por-fecha")
    @ResponseBody
    public List<Venta> getVentasPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        
        return ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
    }

    @GetMapping("/ingresos-por-producto")
    @ResponseBody
    public List<Map<String, Object>> getIngresosPorProducto(@RequestParam(defaultValue = "10") int limite) {
        List<Object[]> resultados = detalleVentaRepository.getIngresosByProducto();
        List<Map<String, Object>> productos = new ArrayList<>();
        
        for (int i = 0; i < Math.min(limite, resultados.size()); i++) {
            Object[] row = resultados.get(i);
            Map<String, Object> producto = new HashMap<>();
            producto.put("id", row[0]);
            producto.put("nombre", row[1]);
            producto.put("totalIngresos", row[2]);
            productos.add(producto);
        }
        
        return productos;
    }

    @GetMapping("/ventas-por-cliente")
    @ResponseBody
    public List<Venta> getVentasPorCliente(@RequestParam String correoCliente) {
        return ventaRepository.findVentasByClienteWithDetalles(correoCliente);
    }

    @GetMapping("/estadisticas-mensuales")
    @ResponseBody
    public Map<String, Object> getEstadisticasMensuales(
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int año,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().getMonthValue()}") int mes) {
        
        Double totalMes = ventaRepository.getTotalVentasByMes(año, mes);
        if (totalMes == null) totalMes = 0.0;
        
        LocalDateTime inicioMes = LocalDate.of(año, mes, 1).atStartOfDay();
        LocalDateTime finMes = LocalDate.of(año, mes, LocalDate.of(año, mes, 1).lengthOfMonth()).atTime(23, 59, 59);
        
        List<Venta> ventasDelMes = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicioMes, finMes);
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("año", año);
        estadisticas.put("mes", mes);
        estadisticas.put("totalIngresos", totalMes);
        estadisticas.put("cantidadVentas", ventasDelMes.size());
        estadisticas.put("promedioVenta", ventasDelMes.isEmpty() ? 0.0 : totalMes / ventasDelMes.size());
        
        return estadisticas;
    }

    @GetMapping("/exportar-pdf")
    public ResponseEntity<byte[]> exportarReportePDF(
            @RequestParam String tipoReporte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "10") int limite) {
        
        try {
            byte[] pdfBytes = reporteVentasService.generarReportePDF(tipoReporte, fechaInicio, fechaFin, limite);
            
            String filename = "reporte_" + tipoReporte + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdfBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportarReporteExcel(
            @RequestParam String tipoReporte,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "10") int limite) {
        
        try {
            byte[] excelBytes = reporteVentasService.generarReporteExcel(tipoReporte, fechaInicio, fechaFin, limite);
            
            String filename = "reporte_" + tipoReporte + "_" + 
                             LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(excelBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Filtrar ventas por usuario cajero
    @GetMapping("/ventas-por-usuario")
    @ResponseBody
    public List<Venta> getVentasPorUsuario(
            @RequestParam String usuarioCajero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            return ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(usuarioCajero, inicio, fin);
        } else {
            return ventaRepository.findByUsuarioCajeroOrderByFechaDesc(usuarioCajero);
        }
    }
    
    // Obtener estadísticas por usuario cajero
    @GetMapping("/estadisticas-usuario")
    @ResponseBody
    public Map<String, Object> getEstadisticasUsuario(@RequestParam String usuarioCajero) {
        Map<String, Object> estadisticas = new HashMap<>();
        
        try {
            // Contar ventas del usuario
            Long totalVentas = ventaRepository.countByUsuarioCajero(usuarioCajero);
            estadisticas.put("totalVentas", totalVentas);
            
            // Calcular ingresos totales del usuario
            Double totalIngresos = ventaRepository.getTotalIngresosByUsuarioCajero(usuarioCajero);
            estadisticas.put("totalIngresos", totalIngresos != null ? totalIngresos : 0.0);
            
            // Ventas del mes actual
            LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            LocalDateTime finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
            List<Venta> ventasDelMes = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
                usuarioCajero, inicioMes, finMes);
            estadisticas.put("ventasDelMes", ventasDelMes.size());
            
            double ingresosDelMes = ventasDelMes.stream().mapToDouble(Venta::getTotal).sum();
            estadisticas.put("ingresosDelMes", ingresosDelMes);
            
            // Ventas del día actual
            LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
            LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);
            List<Venta> ventasDelDia = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
                usuarioCajero, inicioDia, finDia);
            estadisticas.put("ventasDelDia", ventasDelDia.size());
            
            double ingresosDelDia = ventasDelDia.stream().mapToDouble(Venta::getTotal).sum();
            estadisticas.put("ingresosDelDia", ingresosDelDia);
            
        } catch (Exception e) {
            estadisticas.put("error", "Error al cargar estadísticas: " + e.getMessage());
        }
        
        return estadisticas;
    }
    
    // Obtener ventas por usuario con detalles completos
    @GetMapping("/ventas-usuario-detalladas")
    @ResponseBody
    public List<Venta> getVentasUsuarioDetalladas(
            @RequestParam String usuarioCajero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "10") int limite) {
        
        List<Venta> ventas;
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            ventas = ventaRepository.findVentasByUsuarioCajeroAndFechaBetweenWithDetalles(usuarioCajero, inicio, fin);
        } else {
            ventas = ventaRepository.findVentasByUsuarioCajeroWithDetalles(usuarioCajero);
        }
          // Limitar resultados
        return ventas.stream().limit(limite).toList();
    }
    
    // Exportar reporte por usuario en PDF
    @GetMapping("/exportar-pdf-usuario")
    public ResponseEntity<byte[]> exportarReportePDFUsuario(
            @RequestParam String usuarioCajero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            byte[] pdfBytes = reporteVentasService.generarReportePDFPorUsuario(usuarioCajero, fechaInicio, fechaFin);
            
            String filename = "reporte_ventas_usuario_" + usuarioCajero.replaceAll("\\s+", "_") + "_" + 
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
    
    // Exportar reporte por usuario en Excel
    @GetMapping("/exportar-excel-usuario")
    public ResponseEntity<byte[]> exportarReporteExcelUsuario(
            @RequestParam String usuarioCajero,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        try {
            byte[] excelBytes = reporteVentasService.generarReporteExcelPorUsuario(usuarioCajero, fechaInicio, fechaFin);
            
            String filename = "reporte_ventas_usuario_" + usuarioCajero.replaceAll("\\s+", "_") + "_" + 
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

    @GetMapping("/dashboard-datos")
    @ResponseBody
    public Map<String, Object> getDashboardDatos() {
        Map<String, Object> datos = new HashMap<>();
        
        // Estadísticas principales para los contadores del dashboard
        datos.put("totalProductos", (long) productoRepository.findByActivo(1).size()); // Solo productos activos
        datos.put("totalCategorias", categoriaRepository.countByActivoTrue()); // Solo categorías activas
        datos.put("totalVentas", ventaRepository.count());
          // Calcular total de unidades de medida activas en el sistema (no unidades vendidas)
        Long totalUnidades = (long) unidadRepository.findAllActivos().size();
        datos.put("totalUnidades", totalUnidades);
        
        // Ingresos totales
        double totalIngresos = ventaRepository.findAll().stream()
            .mapToDouble(Venta::getTotal)
            .sum();
        datos.put("totalIngresos", totalIngresos);
        
        // Estadísticas del día actual
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);
        List<Venta> ventasHoy = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicioDia, finDia);
        datos.put("ventasHoy", ventasHoy.size());
        datos.put("ingresosHoy", ventasHoy.stream().mapToDouble(Venta::getTotal).sum());
        
        // Estadísticas adicionales para análisis
        datos.put("productosActivos", (long) productoRepository.findByActivo(1).size());
        datos.put("productosInactivos", (long) productoRepository.findByActivo(0).size());
        datos.put("categoriasActivas", categoriaRepository.countByActivoTrue());
        datos.put("categoriasInactivas", (long) categoriaRepository.findByActivo(0).size());
        
        // Productos con stock bajo (menos de 5 unidades)
        List<Producto> productosStockBajo = productoRepository.findByActivoAndStockGreaterThanOrderByNombre(1, 0)
            .stream()
            .filter(producto -> producto.getStock() < 5)
            .toList();
        datos.put("productosStockBajo", productosStockBajo.size());
        
        return datos;
    }
    
    // Endpoint para envío masivo de reportes por correo
    @PostMapping("/enviar-correo-masivo")
    @ResponseBody
    public Map<String, Object> enviarReportePorCorreo(
            @RequestParam String tipoReporte,
            @RequestParam String[] correos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "10") int limite,
            @RequestParam(defaultValue = "pdf") String formato) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Validar que se proporcionen correos
            if (correos == null || correos.length == 0) {
                resultado.put("success", false);
                resultado.put("message", "Debe proporcionar al menos un correo electrónico");
                return resultado;
            }

            // Generar el reporte según el formato solicitado
            byte[] reporteBytes;
            String nombreArchivo;
            String asunto;
            String mensaje;
            
            if ("pdf".equals(formato)) {
                reporteBytes = reporteVentasService.generarReportePDF(tipoReporte, fechaInicio, fechaFin, limite);
                nombreArchivo = "reporte_" + tipoReporte + "_" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            } else {
                reporteBytes = reporteVentasService.generarReporteExcel(tipoReporte, fechaInicio, fechaFin, limite);
                nombreArchivo = "reporte_" + tipoReporte + "_" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            }

            // Configurar asunto y mensaje según el tipo de reporte
            switch (tipoReporte.toLowerCase()) {
                case "productos-mas-vendidos":
                    asunto = "Reporte: Productos Más Vendidos";
                    mensaje = "Adjunto encontrará el reporte de productos más vendidos del sistema.";
                    break;
                case "ventas-por-fecha":
                    asunto = "Reporte: Ventas por Fecha";
                    mensaje = "Adjunto encontrará el reporte de ventas por fecha especificada.";
                    break;
                case "ingresos-por-producto":
                    asunto = "Reporte: Ingresos por Producto";
                    mensaje = "Adjunto encontrará el reporte de ingresos generados por producto.";
                    break;
                case "resumen-general":
                    asunto = "Reporte: Resumen General de Ventas";
                    mensaje = "Adjunto encontrará el resumen general de ventas del sistema.";
                    break;
                default:
                    asunto = "Reporte de Ventas";
                    mensaje = "Adjunto encontrará el reporte solicitado.";
            }

            // Agregar información de fechas al mensaje si están disponibles
            if (fechaInicio != null && fechaFin != null) {
                mensaje += "\n\nPeríodo: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                          " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }

            mensaje += "\n\nEste reporte fue generado automáticamente el " + 
                      LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));

            // Enviar el correo con el reporte adjunto
            emailService.enviarFacturaConAdjunto(correos, asunto, mensaje, reporteBytes, nombreArchivo);

            resultado.put("success", true);
            resultado.put("message", "Reporte enviado exitosamente a " + correos.length + " destinatario(s)");
            resultado.put("destinatarios", correos.length);
            
        } catch (Exception e) {
            resultado.put("success", false);
            resultado.put("message", "Error al enviar el reporte: " + e.getMessage());
        }
        
        return resultado;
    }

    // Endpoint para envío masivo de reportes de usuario por correo
    @PostMapping("/enviar-reporte-usuario-correo")
    @ResponseBody
    public Map<String, Object> enviarReporteUsuarioPorCorreo(
            @RequestParam String usuarioCajero,
            @RequestParam String[] correos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(defaultValue = "pdf") String formato) {
        
        Map<String, Object> resultado = new HashMap<>();
        
        try {
            // Validar que se proporcionen correos
            if (correos == null || correos.length == 0) {
                resultado.put("success", false);
                resultado.put("message", "Debe proporcionar al menos un correo electrónico");
                return resultado;
            }

            // Validar que se proporcione el usuario
            if (usuarioCajero == null || usuarioCajero.trim().isEmpty()) {
                resultado.put("success", false);
                resultado.put("message", "Debe seleccionar un usuario");
                return resultado;
            }

            // Generar el reporte según el formato solicitado
            byte[] reporteBytes;
            String nombreArchivo;
            
            if ("pdf".equals(formato)) {
                reporteBytes = reporteVentasService.generarReportePDFPorUsuario(usuarioCajero, fechaInicio, fechaFin);
                nombreArchivo = "reporte_ventas_usuario_" + usuarioCajero.replaceAll("\\s+", "_") + "_" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            } else {
                reporteBytes = reporteVentasService.generarReporteExcelPorUsuario(usuarioCajero, fechaInicio, fechaFin);
                nombreArchivo = "reporte_ventas_usuario_" + usuarioCajero.replaceAll("\\s+", "_") + "_" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            }

            // Configurar asunto y mensaje
            String asunto = "Reporte de Ventas - Usuario: " + usuarioCajero;
            String mensaje = "Adjunto encontrará el reporte de ventas del usuario " + usuarioCajero + ".";

            // Agregar información de fechas al mensaje si están disponibles
            if (fechaInicio != null && fechaFin != null) {
                mensaje += "\n\nPeríodo: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                          " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } else {
                mensaje += "\n\nPeríodo: Todas las ventas registradas";
            }

            mensaje += "\n\nEste reporte fue generado automáticamente el " + 
                      LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm"));

            // Enviar el correo con el reporte adjunto
            emailService.enviarFacturaConAdjunto(correos, asunto, mensaje, reporteBytes, nombreArchivo);

            resultado.put("success", true);
            resultado.put("message", "Reporte de " + usuarioCajero + " enviado exitosamente a " + correos.length + " destinatario(s)");
            resultado.put("destinatarios", correos.length);
            resultado.put("usuario", usuarioCajero);
            
        } catch (Exception e) {
            resultado.put("success", false);
            resultado.put("message", "Error al enviar el reporte: " + e.getMessage());
        }
          return resultado;
    }

    /**
     * Generar y descargar factura individual para una venta específica
     */
    @GetMapping("/generar-factura")
    public ResponseEntity<byte[]> generarFactura(@RequestParam("ventaId") Long ventaId) {
        try {
            // Buscar la venta
            Optional<Venta> ventaOpt = ventaRepository.findById(ventaId);
            if (!ventaOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Venta venta = ventaOpt.get();
            
            // Generar la factura en PDF
            byte[] facturaBytes = reporteVentasService.generarFacturaIndividualPDF(venta);
            
            // Configurar nombre del archivo
            String nombreArchivo = "Factura_Venta_" + ventaId + "_" + 
                                  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf";
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", nombreArchivo);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(facturaBytes);
                    
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
