package com.scprojectjava2.controller;

import com.scprojectjava2.model.Producto;
import com.scprojectjava2.model.Categoria;
import com.scprojectjava2.model.Unidad;
import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.model.HistorialStock;
import com.scprojectjava2.model.OperacionCargaMasiva;
import com.scprojectjava2.model.DetalleCargaMasiva;
import com.scprojectjava2.service.ProductoService;
import com.scprojectjava2.service.CategoriaService;
import com.scprojectjava2.service.UnidadService;
import com.scprojectjava2.service.HistorialStockService;
import com.scprojectjava2.service.CargaMasivaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private UnidadService unidadService;
    
    @Autowired
    private HistorialStockService historialStockService;

    @GetMapping
    public String listarProductos(@RequestParam(name = "accion", required = false) String accion,
                                   @RequestParam(name = "id", required = false) Integer id,
                                   Model model, HttpSession session) {
        
        // Obtener usuario de la sesión para verificación de roles en la vista
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        
        // Inicializar por defecto la variable viendoInactivos como false
        model.addAttribute("viendoInactivos", false);

        if ("editar".equals(accion) && id != null) {
            Producto productoEditar = productoService.obtenerPorId(id);
            model.addAttribute("productoEditar", productoEditar);
        }

        if ("verInactivos".equals(accion)) {
            List<Producto> productosInactivos = productoService.listarInactivos();
            model.addAttribute("productos", productosInactivos);
            model.addAttribute("viendoInactivos", true);
        } else {
            List<Producto> productos = productoService.listarActivos();
            model.addAttribute("productos", productos);
        }

        // Enviar listas de categorías y unidades para los selects
        List<Categoria> categorias = categoriaService.listarActivas();
        List<Unidad> unidades = unidadService.listarActivas();

        model.addAttribute("categorias", categorias);
        model.addAttribute("unidades", unidades);

        return "productos"; // productos.html (Thymeleaf)
    }

    @PostMapping
    public String guardarProducto(@ModelAttribute Producto producto,
                                 @RequestParam(name = "categoria_id", required = false) Integer categoriaId,
                                 @RequestParam(name = "unidad_id", required = false) Integer unidadId,
                                 @RequestParam(name = "iva", required = false) Double iva,
                                 @RequestParam(name = "fechaVencimiento", required = false) String fechaVencimientoStr,
                                 Model model, HttpSession session) {
        
        // Obtener usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        // Validar que el código no esté vacío
        if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
            model.addAttribute("error", "El código del producto es obligatorio");
            model.addAttribute("usuario", usuario);
            
            // Asignar categoría y unidad al producto para mostrar correctamente en el formulario
            if (categoriaId != null) {
                Categoria categoria = categoriaService.obtenerPorId(categoriaId);
                producto.setCategoria(categoria);
            }
            if (unidadId != null) {
                Unidad unidad = unidadService.obtenerPorId(unidadId);
                producto.setUnidad(unidad);
            }
            
            model.addAttribute("productoEditar", producto);
            
            // Recargar datos para el formulario
            List<Producto> productos = productoService.listarActivos();
            List<Categoria> categorias = categoriaService.listarActivas();
            List<Unidad> unidades = unidadService.listarActivas();
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("unidades", unidades);
            model.addAttribute("viendoInactivos", false);
            
            return "productos";
        }
        
        // Validar que el precio no sea negativo
        if (producto.getPrecio() <= 0) {
            model.addAttribute("error", "El precio del producto debe ser mayor a 0");
            model.addAttribute("usuario", usuario);
            
            // Asignar categoría y unidad al producto para mostrar correctamente en el formulario
            if (categoriaId != null) {
                Categoria categoria = categoriaService.obtenerPorId(categoriaId);
                producto.setCategoria(categoria);
            }
            if (unidadId != null) {
                Unidad unidad = unidadService.obtenerPorId(unidadId);
                producto.setUnidad(unidad);
            }
            
            model.addAttribute("productoEditar", producto);
            
            // Recargar datos para el formulario
            List<Producto> productos = productoService.listarActivos();
            List<Categoria> categorias = categoriaService.listarActivas();
            List<Unidad> unidades = unidadService.listarActivas();
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("unidades", unidades);
            model.addAttribute("viendoInactivos", false);
            
            return "productos";
        }
        
        // Validar que el stock inicial no sea 0 para productos nuevos
        Integer id = producto.getId();
        if (id == null || id == 0) {
            // Es un nuevo producto - validar stock inicial
            if (producto.getStock() <= 0) {
                model.addAttribute("error", "El stock inicial debe ser mayor a 0 para productos nuevos");
                model.addAttribute("usuario", usuario);
                
                // Asignar categoría y unidad al producto para mostrar correctamente en el formulario
                if (categoriaId != null) {
                    Categoria categoria = categoriaService.obtenerPorId(categoriaId);
                    producto.setCategoria(categoria);
                }
                if (unidadId != null) {
                    Unidad unidad = unidadService.obtenerPorId(unidadId);
                    producto.setUnidad(unidad);
                }
                
                model.addAttribute("productoEditar", producto);
                
                // Recargar datos para el formulario
                List<Producto> productos = productoService.listarActivos();
                List<Categoria> categorias = categoriaService.listarActivas();
                List<Unidad> unidades = unidadService.listarActivas();
                model.addAttribute("productos", productos);
                model.addAttribute("categorias", categorias);
                model.addAttribute("unidades", unidades);
                model.addAttribute("viendoInactivos", false);
                
                return "productos";
            }
        }
        
        // Verificar si es una actualización o un nuevo registro
        // Integer id = producto.getId(); // Ya declarado arriba
        
        // Validar unicidad del código
        if (id != null && id > 0) {
            // Es una actualización - verificar que el código no lo use otro producto
            if (productoService.existeCodigoParaOtroProducto(producto.getCodigo().trim(), id)) {
                model.addAttribute("error", "Ya existe otro producto con el código: " + producto.getCodigo());
                model.addAttribute("usuario", usuario);
                
                // Asignar categoría y unidad al producto para mostrar correctamente en el formulario
                if (categoriaId != null) {
                    Categoria categoria = categoriaService.obtenerPorId(categoriaId);
                    producto.setCategoria(categoria);
                }
                if (unidadId != null) {
                    Unidad unidad = unidadService.obtenerPorId(unidadId);
                    producto.setUnidad(unidad);
                }
                
                model.addAttribute("productoEditar", producto);
                
                // Recargar datos para el formulario
                List<Producto> productos = productoService.listarActivos();
                List<Categoria> categorias = categoriaService.listarActivas();
                List<Unidad> unidades = unidadService.listarActivas();
                
                // Debug logging
                System.out.println("DEBUG - Error duplicado código actualización producto:");
                System.out.println("Productos size: " + (productos != null ? productos.size() : "null"));
                System.out.println("Categorias size: " + (categorias != null ? categorias.size() : "null"));
                System.out.println("Unidades size: " + (unidades != null ? unidades.size() : "null"));
                
                model.addAttribute("productos", productos);
                model.addAttribute("categorias", categorias);
                model.addAttribute("unidades", unidades);
                model.addAttribute("viendoInactivos", false);
                
                return "productos";
            }
        } else {
            // Es un nuevo producto - verificar que el código no exista
            if (productoService.existeCodigo(producto.getCodigo().trim())) {
                model.addAttribute("error", "Ya existe un producto con el código: " + producto.getCodigo());
                model.addAttribute("usuario", usuario);
                
                // Asignar categoría y unidad al producto para mostrar correctamente en el formulario
                if (categoriaId != null) {
                    Categoria categoria = categoriaService.obtenerPorId(categoriaId);
                    producto.setCategoria(categoria);
                }
                if (unidadId != null) {
                    Unidad unidad = unidadService.obtenerPorId(unidadId);
                    producto.setUnidad(unidad);
                }
                
                model.addAttribute("productoEditar", producto);
                
                // Recargar datos para el formulario
                List<Producto> productos = productoService.listarActivos();
                List<Categoria> categorias = categoriaService.listarActivas();
                List<Unidad> unidades = unidadService.listarActivas();
                
                // Debug logging
                System.out.println("DEBUG - Error duplicado código nuevo producto:");
                System.out.println("Productos size: " + (productos != null ? productos.size() : "null"));
                System.out.println("Categorias size: " + (categorias != null ? categorias.size() : "null"));
                System.out.println("Unidades size: " + (unidades != null ? unidades.size() : "null"));
                
                model.addAttribute("productos", productos);
                model.addAttribute("categorias", categorias);
                model.addAttribute("unidades", unidades);
                model.addAttribute("viendoInactivos", false);
                
                return "productos";
            }
        }
        
        // Asignar categoría y unidad basados en los IDs proporcionados
        if (categoriaId != null) {
            Categoria categoria = categoriaService.obtenerPorId(categoriaId);
            producto.setCategoria(categoria);
        }
        
        if (unidadId != null) {
            Unidad unidad = unidadService.obtenerPorId(unidadId);
            producto.setUnidad(unidad);
        }
        
        // Asignar IVA si se proporciona
        if (iva != null && iva >= 0) {
            producto.setIva(iva);
        } else {
            producto.setIva(null);
        }
        
        // Asignar fecha de vencimiento si se proporciona
        if (fechaVencimientoStr != null && !fechaVencimientoStr.trim().isEmpty()) {
            try {
                producto.setFechaVencimiento(java.time.LocalDate.parse(fechaVencimientoStr));
            } catch (Exception e) {
                producto.setFechaVencimiento(null);
            }
        } else {
            producto.setFechaVencimiento(null);
        }
        
        // Guardar el producto
        if (id != null && id > 0) {
            productoService.actualizar(producto);
        } else {
            productoService.agregar(producto);
        }
        return "redirect:/productos";
    }

    @PostMapping("/actualizar")
    public String actualizarProducto(@ModelAttribute Producto producto,
                                   @RequestParam(name = "categoria_id", required = false) Integer categoriaId,
                                   @RequestParam(name = "unidad_id", required = false) Integer unidadId,
                                   @RequestParam(name = "iva", required = false) Double iva,
                                   @RequestParam(name = "fechaVencimiento", required = false) String fechaVencimientoStr,
                                   Model model, HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        
        // Obtener usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (!usuario.isAdministrador()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para actualizar productos");
            return "redirect:/productos";
        }

        try {
            // Obtener el producto actual para mantener el stock sin cambios
            Producto productoActual = productoService.obtenerPorId(producto.getId());
            if (productoActual == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/productos";
            }

            // Mantener el stock actual - el stock solo se modifica a través de los endpoints específicos
            producto.setStock(productoActual.getStock());
            
            // Validar que el código no esté vacío
            if (producto.getCodigo() == null || producto.getCodigo().trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "El código del producto es obligatorio");
                return "redirect:/productos/editar/" + producto.getId();
            }

            // Validar unicidad del código
            if (productoService.existeCodigoParaOtroProducto(producto.getCodigo().trim(), producto.getId())) {
                redirectAttributes.addFlashAttribute("error", "Ya existe otro producto con el código: " + producto.getCodigo());
                return "redirect:/productos/editar/" + producto.getId();
            }

            // Procesar fecha de vencimiento
            if (fechaVencimientoStr != null && !fechaVencimientoStr.trim().isEmpty()) {
                try {
                    LocalDate fechaVencimiento = LocalDate.parse(fechaVencimientoStr);
                    producto.setFechaVencimiento(fechaVencimiento);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Formato de fecha de vencimiento inválido");
                    return "redirect:/productos/editar/" + producto.getId();
                }
            }

            // Asignar categoría y unidad
            if (categoriaId != null) {
                Categoria categoria = categoriaService.obtenerPorId(categoriaId);
                producto.setCategoria(categoria);
            }
            if (unidadId != null) {
                Unidad unidad = unidadService.obtenerPorId(unidadId);
                producto.setUnidad(unidad);
            }

            // Asignar IVA
            producto.setIva(iva);

            // Obtener producto existente para mantener el stock
            Producto productoExistente = productoService.obtenerPorId(producto.getId());
            producto.setStock(productoExistente.getStock()); // Mantener stock actual

            // Actualizar producto (sin cambiar stock)
            productoService.actualizar(producto);

            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado correctamente");
            return "redirect:/productos/editar/" + producto.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el producto: " + e.getMessage());
            return "redirect:/productos/editar/" + producto.getId();
        }
    }

    @PostMapping("/eliminar")
    public String eliminarProducto(@RequestParam("id") int id, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (!usuario.isAdministrador()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para eliminar productos");
            return "redirect:/productos";
        }
        
        try {
            productoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Producto eliminado exitosamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el producto: " + e.getMessage());
        }
        
        return "redirect:/productos";
    }

    @GetMapping("/reactivar/{id}")
    public String reactivarProducto(@PathVariable("id") int id) {
        productoService.reactivar(id);
        return "redirect:/productos?accion=verInactivos";
    }
    
    @GetMapping("/reporte")
    public void generarReportePDF(HttpServletResponse response) throws DocumentException, IOException {
        response.setContentType("application/pdf");
        
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = dateFormatter.format(new Date());
        
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=productos_" + fechaActual + ".pdf";
        
        response.setHeader(cabecera, valor);
        
        Document documento = new Document(PageSize.A4);
        PdfWriter.getInstance(documento, response.getOutputStream());
        
        documento.open();
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitulo.setSize(18);
        
        Paragraph titulo = new Paragraph("Lista de Productos", fontTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        documento.add(titulo);
        
        PdfPTable tabla = new PdfPTable(8);
        tabla.setWidthPercentage(100f);
        tabla.setWidths(new float[] {1.0f, 2.5f, 1.5f, 1.0f, 1.5f, 1.5f, 1.5f, 1.5f});
        tabla.setSpacingBefore(10);
        
        escribirCabeceraDeLaTabla(tabla);
        escribirDatosDeLaTabla(tabla);
        
        documento.add(tabla);
        documento.close();
    }
    
    private void escribirCabeceraDeLaTabla(PdfPTable tabla) {
        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(java.awt.Color.BLUE);
        celda.setPadding(5);
        
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA);
        fuente.setColor(java.awt.Color.WHITE);
        
        celda.setPhrase(new Phrase("Código", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Nombre", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Precio", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("IVA (%)", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Precio + IVA", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Stock", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Categoría", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Vencimiento", fuente));
        tabla.addCell(celda);
    }
    
    private void escribirDatosDeLaTabla(PdfPTable tabla) {
        List<Producto> productos = productoService.listarActivos();
        
        for (Producto producto : productos) {
            tabla.addCell(producto.getCodigo());
            tabla.addCell(producto.getNombre());
            tabla.addCell(String.format("S/. %.2f", producto.getPrecio()));
            
            // IVA
            if (producto.getIva() != null) {
                tabla.addCell(String.format("%.2f%%", producto.getIva()));
            } else {
                tabla.addCell("N/A");
            }
            
            // Precio con IVA
            if (producto.getIva() != null) {
                tabla.addCell(String.format("S/. %.2f", producto.getPrecioConIva()));
            } else {
                tabla.addCell(String.format("S/. %.2f", producto.getPrecio()));
            }
            
            tabla.addCell(String.valueOf(producto.getStock()));
            tabla.addCell(producto.getCategoria().getNombre());
            
            // Fecha de vencimiento
            if (producto.getFechaVencimiento() != null) {
                tabla.addCell(producto.getFechaVencimiento().toString());
            } else {
                tabla.addCell("N/A");
            }
        }
    }
    
    // Nuevo método para generar reporte PDF por fechas
    @GetMapping("/reporte-fechas")
    public void generarReportePDFPorFechas(
            @RequestParam("fechaInicio") String fechaInicioStr,
            @RequestParam("fechaFin") String fechaFinStr,
            HttpServletResponse response) throws DocumentException, IOException {
        
        // Parsear las fechas
        LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
        LocalDate fechaFin = LocalDate.parse(fechaFinStr);
        
        response.setContentType("application/pdf");
        
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = dateFormatter.format(new Date());
        
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=productos_stock_" + fechaInicioStr + "_" + fechaFinStr + "_" + fechaActual + ".pdf";
        
        response.setHeader(cabecera, valor);
        
        Document documento = new Document(PageSize.A4.rotate()); // Landscape para más columnas
        PdfWriter.getInstance(documento, response.getOutputStream());
        
        documento.open();
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
        fontTitulo.setSize(18);
        
        Paragraph titulo = new Paragraph("Reporte de Stock por Fechas", fontTitulo);
        titulo.setAlignment(Paragraph.ALIGN_CENTER);
        documento.add(titulo);
        
        // Agregar información del período
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA);
        fontSubtitulo.setSize(12);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        Paragraph periodo = new Paragraph("Período: " + fechaInicio.format(formatter) + " - " + fechaFin.format(formatter), fontSubtitulo);
        periodo.setAlignment(Paragraph.ALIGN_CENTER);
        periodo.setSpacingAfter(15);
        documento.add(periodo);
        
        // Crear tabla con más columnas para el historial
        PdfPTable tabla = new PdfPTable(9);
        tabla.setWidthPercentage(100f);
        tabla.setWidths(new float[] {1.2f, 2.5f, 1.3f, 1.3f, 1.3f, 1.3f, 1.3f, 1.5f, 1.5f});
        tabla.setSpacingBefore(10);
        
        escribirCabeceraTablaFechas(tabla);
        escribirDatosTablaFechas(tabla, fechaInicio, fechaFin);
        
        documento.add(tabla);
        documento.close();
    }
    
    private void escribirCabeceraTablaFechas(PdfPTable tabla) {
        PdfPCell celda = new PdfPCell();
        celda.setBackgroundColor(java.awt.Color.BLUE);
        celda.setPadding(5);
        
        Font fuente = FontFactory.getFont(FontFactory.HELVETICA);
        fuente.setColor(java.awt.Color.WHITE);
        
        celda.setPhrase(new Phrase("Código", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Nombre", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Stock Inicial", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Entradas", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Salidas", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Stock Final", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Variación", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Categoría", fuente));
        tabla.addCell(celda);
        
        celda.setPhrase(new Phrase("Precio", fuente));
        tabla.addCell(celda);
    }
    
    private void escribirDatosTablaFechas(PdfPTable tabla, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<Integer, HistorialStockService.StockEnFecha> stockPorFechas = 
            historialStockService.obtenerStockPorFechas(fechaInicio, fechaFin);
        
        // Si no hay datos de historial, mostrar todos los productos activos con stock actual
        if (stockPorFechas.isEmpty()) {
            List<Producto> productos = productoService.listarActivos();
            for (Producto producto : productos) {
                tabla.addCell(producto.getCodigo());
                tabla.addCell(producto.getNombre());
                tabla.addCell(String.valueOf(producto.getStock())); // Stock inicial = stock actual
                tabla.addCell("0"); // Entradas
                tabla.addCell("0"); // Salidas
                tabla.addCell(String.valueOf(producto.getStock())); // Stock final = stock actual
                tabla.addCell("0"); // Variación
                tabla.addCell(producto.getCategoria().getNombre());
                tabla.addCell(String.format("S/. %.2f", producto.getPrecio()));
            }
        } else {
            for (HistorialStockService.StockEnFecha stockInfo : stockPorFechas.values()) {
                Producto producto = stockInfo.getProducto();
                
                tabla.addCell(producto.getCodigo());
                tabla.addCell(producto.getNombre());
                tabla.addCell(String.valueOf(stockInfo.getStockInicial()));
                tabla.addCell(String.valueOf(stockInfo.getEntradas()));
                tabla.addCell(String.valueOf(stockInfo.getSalidas()));
                tabla.addCell(String.valueOf(stockInfo.getStockFinal()));
                
                // Variación con color
                Integer variacion = stockInfo.getVariacion();
                PdfPCell celdaVariacion = new PdfPCell(new Phrase(String.valueOf(variacion)));
                if (variacion > 0) {
                    celdaVariacion.setBackgroundColor(new java.awt.Color(200, 255, 200)); // Verde claro
                } else if (variacion < 0) {
                    celdaVariacion.setBackgroundColor(new java.awt.Color(255, 200, 200)); // Rojo claro
                }
                tabla.addCell(celdaVariacion);
                
                tabla.addCell(producto.getCategoria().getNombre());
                tabla.addCell(String.format("S/. %.2f", producto.getPrecio()));
            }
        }
    }
    
    @Autowired
    private CargaMasivaService cargaMasivaService;

    @GetMapping("/carga-masiva")
    public String mostrarFormularioCargaMasiva(Model model) {
        List<Categoria> categorias = categoriaService.listarActivas();
        List<Unidad> unidades = unidadService.listarActivas();
        
        model.addAttribute("categorias", categorias);
        model.addAttribute("unidades", unidades);
        
        return "productos-carga-masiva";
    }
    
    @GetMapping("/plantilla-csv")
    public ResponseEntity<Resource> descargarPlantilla() throws IOException {
        Resource resource = new ClassPathResource("static/plantilla-productos.csv");
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=plantilla-productos.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
    }
    
    @PostMapping("/carga-masiva")
    public String procesarCargaMasiva(@RequestParam("archivo") MultipartFile archivo, 
                                     Model model, HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        // Obtener usuario de la sesión
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }

        if (!usuario.isAdministrador()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para realizar carga masiva");
            return "redirect:/productos/carga-masiva";
        }

        if (archivo.isEmpty()) {
            model.addAttribute("error", "No se ha seleccionado un archivo o el archivo está vacío");
            return mostrarFormularioCargaMasiva(model);
        }

        try {
            // Contar líneas del archivo para inicializar la operación
            int totalLineas = 0;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream()))) {
                reader.readLine(); // Saltar encabezados
                while (reader.readLine() != null) {
                    totalLineas++;
                }
            }

            if (totalLineas == 0) {
                model.addAttribute("error", "El archivo no contiene datos para procesar");
                return mostrarFormularioCargaMasiva(model);
            }

            // Iniciar operación de carga masiva
            OperacionCargaMasiva operacion = cargaMasivaService.iniciarOperacion(
                archivo.getOriginalFilename(), usuario.getNombreUsuario(), totalLineas);

            // Procesar archivo línea por línea
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream()))) {
                String linea;
                int numeroLinea = 0;
                boolean primeraLinea = true;

                while ((linea = reader.readLine()) != null) {
                    numeroLinea++;
                    
                    // Saltar la primera línea (encabezados)
                    if (primeraLinea) {
                        primeraLinea = false;
                        continue;
                    }

                    String[] campos = linea.split(",");
                    
                    // Extraer campos básicos
                    String codigo = campos.length > 0 ? campos[0].trim() : "";
                    String nombre = campos.length > 1 ? campos[1].trim() : "";
                    String descripcion = campos.length > 2 ? campos[2].trim() : "";
                    
                    Double precio = null;
                    Integer stock = null;
                    String categoriaNombre = campos.length > 5 ? campos[5].trim() : "";
                    String unidadNombre = campos.length > 6 ? campos[6].trim() : "";
                    Double iva = null;
                    String fechaVencimiento = null;

                    // Parsear campos numéricos
                    try {
                        if (campos.length > 3 && !campos[3].trim().isEmpty()) {
                            precio = Double.parseDouble(campos[3].trim());
                        }
                        if (campos.length > 4 && !campos[4].trim().isEmpty()) {
                            stock = Integer.parseInt(campos[4].trim());
                        }
                        if (campos.length > 7 && !campos[7].trim().isEmpty()) {
                            iva = Double.parseDouble(campos[7].trim());
                        }
                        if (campos.length > 8 && !campos[8].trim().isEmpty()) {
                            fechaVencimiento = campos[8].trim();
                        }
                    } catch (NumberFormatException e) {
                        // Los errores de parsing se manejarán en el servicio
                    }

                    // Procesar producto
                    cargaMasivaService.procesarProducto(operacion, numeroLinea, codigo, nombre, 
                                                      descripcion, precio, stock, categoriaNombre, 
                                                      unidadNombre, iva, fechaVencimiento);
                }
            }

            // Finalizar operación
            String observaciones = String.format("Procesados: %d/%d productos", 
                                                operacion.getProductosExitosos(), operacion.getTotalProductos());
            cargaMasivaService.finalizarOperacion(operacion, observaciones);

            // Preparar datos para la vista
            model.addAttribute("operacionCompletada", true);
            model.addAttribute("operacion", operacion);
            model.addAttribute("mensaje", String.format("Carga masiva %s. Productos exitosos: %d, Fallidos: %d", 
                                                       operacion.getEstado().toLowerCase(), 
                                                       operacion.getProductosExitosos(), 
                                                       operacion.getProductosFallidos()));

            // Obtener detalles para mostrar errores si los hay
            if (operacion.getProductosFallidos() > 0) {
                List<DetalleCargaMasiva> detallesFallidos = cargaMasivaService.obtenerDetallesFallidos(operacion.getId());
                model.addAttribute("detallesFallidos", detallesFallidos);
            }

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar el archivo: " + e.getMessage());
        }

        return mostrarFormularioCargaMasiva(model);
    }
    
    @GetMapping("/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable("id") Integer id, Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (!usuario.isAdministrador()) {
            model.addAttribute("error", "No tienes permisos para editar productos");
            return "redirect:/productos";
        }

        try {
            Producto producto = productoService.obtenerPorId(id);
            if (producto == null) {
                model.addAttribute("error", "Producto no encontrado");
                return "redirect:/productos";
            }

            // Cargar datos necesarios para el formulario
            List<Categoria> categorias = categoriaService.listarActivas();
            List<Unidad> unidades = unidadService.listarActivas();
            
            // Obtener historial de stock
            List<HistorialStock> historialStock = historialStockService.obtenerHistorialPorProducto(id);
            
            // Verificar que el historial no tenga datos nulos o problemáticos
            if (historialStock != null) {
                historialStock = historialStock.stream()
                    .filter(h -> h != null && h.getFechaMovimiento() != null)
                    .collect(java.util.stream.Collectors.toList());
            }

            model.addAttribute("producto", producto);
            model.addAttribute("categorias", categorias);
            model.addAttribute("unidades", unidades);
            model.addAttribute("historial", historialStock);
            model.addAttribute("usuario", usuario);

        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar el producto: " + e.getMessage());
            return "redirect:/productos";
        }

        return "producto-editar";
    }
    
    @PostMapping("/stock/ingreso")
    public String registrarIngresoStock(@RequestParam("productoId") Integer productoId,
                                      @RequestParam("cantidad") Integer cantidad,
                                      @RequestParam("motivo") String motivo,
                                      @RequestParam(value = "motivoDetalle", required = false) String motivoDetalle,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (!usuario.isAdministrador()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar stock");
            return "redirect:/productos";
        }

        try {
            Producto producto = productoService.obtenerPorId(productoId);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/productos";
            }

            if (cantidad == null || cantidad <= 0) {
                redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a 0");
                return "redirect:/productos/editar/" + productoId;
            }

            // Usar motivo detallado si se especificó "Otro"
            String motivoFinal = "Otro".equals(motivo) && motivoDetalle != null && !motivoDetalle.trim().isEmpty() 
                               ? motivoDetalle.trim() : motivo;

            // Calcular nuevo stock
            Integer stockAnterior = producto.getStock();
            Integer nuevoStock = stockAnterior + cantidad;

            // Actualizar stock
            producto.setStock(nuevoStock);
            productoService.actualizar(producto);

            // Registrar movimiento en historial
            historialStockService.registrarMovimiento(
                productoId, stockAnterior, nuevoStock, 
                "ENTRADA", motivoFinal, usuario.getNombre()
            );

            redirectAttributes.addFlashAttribute("mensaje", 
                "Ingreso registrado: +" + cantidad + " unidades. Stock actual: " + nuevoStock);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar ingreso: " + e.getMessage());
        }

        return "redirect:/productos/editar/" + productoId;
    }

    @PostMapping("/stock/egreso")
    public String registrarEgresoStock(@RequestParam("productoId") Integer productoId,
                                     @RequestParam("cantidad") Integer cantidad,
                                     @RequestParam("motivo") String motivo,
                                     @RequestParam(value = "motivoDetalle", required = false) String motivoDetalle,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        
        if (!usuario.isAdministrador()) {
            redirectAttributes.addFlashAttribute("error", "No tienes permisos para modificar stock");
            return "redirect:/productos";
        }

        try {
            Producto producto = productoService.obtenerPorId(productoId);
            if (producto == null) {
                redirectAttributes.addFlashAttribute("error", "Producto no encontrado");
                return "redirect:/productos";
            }

            if (cantidad == null || cantidad <= 0) {
                redirectAttributes.addFlashAttribute("error", "La cantidad debe ser mayor a 0");
                return "redirect:/productos/editar/" + productoId;
            }

            if (cantidad > producto.getStock()) {
                redirectAttributes.addFlashAttribute("error", 
                    "No hay suficiente stock. Stock actual: " + producto.getStock());
                return "redirect:/productos/editar/" + productoId;
            }

            // Usar motivo detallado si se especificó "Otro"
            String motivoFinal = "Otro".equals(motivo) && motivoDetalle != null && !motivoDetalle.trim().isEmpty() 
                               ? motivoDetalle.trim() : motivo;

            // Calcular nuevo stock
            Integer stockAnterior = producto.getStock();
            Integer nuevoStock = stockAnterior - cantidad;

            // Actualizar stock
            producto.setStock(nuevoStock);
            productoService.actualizar(producto);

            // Registrar movimiento en historial
            historialStockService.registrarMovimiento(
                productoId, stockAnterior, nuevoStock, 
                "SALIDA", motivoFinal, usuario.getNombre()
            );

            redirectAttributes.addFlashAttribute("mensaje", 
                "Egreso registrado: -" + cantidad + " unidades. Stock actual: " + nuevoStock);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al registrar egreso: " + e.getMessage());
        }

        return "redirect:/productos/editar/" + productoId;
    }
}
