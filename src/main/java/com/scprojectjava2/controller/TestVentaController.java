package com.scprojectjava2.controller;

import com.scprojectjava2.model.Categoria;
import com.scprojectjava2.model.Producto;
import com.scprojectjava2.model.Venta;
import com.scprojectjava2.model.DetalleVenta;
import com.scprojectjava2.repository.CategoriaRepository;
import com.scprojectjava2.repository.ProductoRepository;
import com.scprojectjava2.repository.VentaRepository;
import com.scprojectjava2.service.EmailService;
import com.scprojectjava2.service.HistorialStockService;
import com.scprojectjava2.utils.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/test-ventas")
public class TestVentaController {
    private static final Logger logger = LoggerFactory.getLogger(TestVentaController.class);

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private VentaRepository ventaRepository;    @Autowired
    private EmailService emailService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private HistorialStockService historialStockService;@GetMapping
    public String mostrarFormularioVentas(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoria", required = false) Integer categoriaId,
            Model model) {
        try {
            List<Producto> productos;
            List<Categoria> categorias = categoriaRepository.findByActivoTrueOrderByNombre();
              // Aplicar filtros de búsqueda
            if (busqueda != null && !busqueda.trim().isEmpty() && categoriaId != null && categoriaId > 0) {
                // Búsqueda por nombre Y categoría
                productos = productoRepository.findByNombreContainingIgnoreCaseAndCategoriaIdAndActivoOrderByNombre(busqueda.trim(), categoriaId, 1);
            } else if (busqueda != null && !busqueda.trim().isEmpty()) {
                // Solo búsqueda por nombre
                productos = productoRepository.findByNombreContainingIgnoreCaseAndActivoTrueOrderByNombre(busqueda.trim());
            } else if (categoriaId != null && categoriaId > 0) {
                // Solo búsqueda por categoría
                productos = productoRepository.findByCategoriaIdAndActivoTrueOrderByNombre(categoriaId);
            } else {
                // Sin filtros, mostrar todos los productos activos
                productos = productoRepository.findByActivoTrueOrderByNombre();
            }
            
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("busquedaActual", busqueda);
            model.addAttribute("categoriaActual", categoriaId);
            
            // Agregar información de resultados
            if (busqueda != null || categoriaId != null) {
                model.addAttribute("resultadosBusqueda", productos.size());
            }
            
            return "ventas";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
            return "ventas";
        }
    }
      @GetMapping("/buscar-ajax")
    public String buscarProductosAjax(
            @RequestParam(value = "busqueda", required = false) String busqueda,
            @RequestParam(value = "categoria", required = false) Integer categoriaId,
            Model model) {
        try {
            List<Producto> productos;
            
            // Aplicar filtros de búsqueda
            if (busqueda != null && !busqueda.trim().isEmpty() && categoriaId != null && categoriaId > 0) {
                // Búsqueda por código, nombre o descripción Y categoría
                productos = productoRepository.findByCodigoOrNombreOrDescripcionContainingIgnoreCaseAndActivo(busqueda.trim(), 1)
                    .stream()
                    .filter(p -> p.getCategoria().getId().equals(categoriaId))
                    .collect(java.util.stream.Collectors.toList());
            } else if (busqueda != null && !busqueda.trim().isEmpty()) {
                // Solo búsqueda por código, nombre o descripción
                productos = productoRepository.findByCodigoOrNombreOrDescripcionContainingIgnoreCaseAndActivo(busqueda.trim(), 1);
            } else if (categoriaId != null && categoriaId > 0) {
                // Solo búsqueda por categoría
                productos = productoRepository.findByCategoriaIdAndActivoTrueOrderByNombre(categoriaId);
            } else {
                // Sin filtros, mostrar todos los productos activos
                productos = productoRepository.findByActivoTrueOrderByNombre();
            }
            
            model.addAttribute("productos", productos);
            model.addAttribute("busquedaActual", busqueda);
            model.addAttribute("categoriaActual", categoriaId);
            
            // Agregar información de resultados
            model.addAttribute("resultadosBusqueda", productos.size());
            
            // Retornar solo el fragmento con los productos, no toda la página
            return "fragments/productos-lista :: productos-lista";
        } catch (Exception e) {
            logger.error("Error al buscar productos por AJAX", e);
            model.addAttribute("error", "Error al buscar productos: " + e.getMessage());
            return "fragments/productos-lista :: productos-lista";
        }
    }    @PostMapping
    public String procesarVenta(
            @RequestParam(value = "correos", required = false) String[] correos,
            @RequestParam(value = "nombreCliente", required = false) String nombreCliente,
            @RequestParam(value = "productos", required = false) List<String> productosStr,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
        try {
            logger.info("Processing sales form submission...");
            
            // Obtener usuario de la sesión
            com.scprojectjava2.model.Usuario usuario = (com.scprojectjava2.model.Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado");
                return "redirect:/login";
            }
            
            // Log all received parameters for debugging
            java.util.Map<String, String[]> paramMap = request.getParameterMap();
            logger.info("All received parameters:");
            for (String paramName : paramMap.keySet()) {
                String[] values = paramMap.get(paramName);
                logger.info("  {} = {}", paramName, java.util.Arrays.toString(values));
            }
            
            // Extract product data from request parameters
            List<Long> productoIds = new ArrayList<>();
            List<Integer> cantidades = new ArrayList<>();
            List<Double> preciosUnitarios = new ArrayList<>();
            
            // Find all product parameters by iterating through the parameter map
            for (String paramName : paramMap.keySet()) {
                if (paramName.matches("productos\\[\\d+\\]\\.productoId")) {
                    String[] values = paramMap.get(paramName);
                    if (values.length > 0 && !values[0].isEmpty()) {
                        productoIds.add(Long.parseLong(values[0]));
                    }
                } else if (paramName.matches("productos\\[\\d+\\]\\.cantidad")) {
                    String[] values = paramMap.get(paramName);
                    if (values.length > 0 && !values[0].isEmpty()) {
                        cantidades.add(Integer.parseInt(values[0]));
                    }
                } else if (paramName.matches("productos\\[\\d+\\]\\.precioUnitario")) {
                    String[] values = paramMap.get(paramName);
                    if (values.length > 0 && !values[0].isEmpty()) {
                        preciosUnitarios.add(Double.parseDouble(values[0]));
                    }
                }
            }
            
            logger.info("Extracted {} products, {} quantities, {} prices", 
                productoIds.size(), cantidades.size(), preciosUnitarios.size());
            
            if (productoIds.isEmpty() || cantidades.isEmpty() || preciosUnitarios.isEmpty()) {
                throw new RuntimeException("No se encontraron productos válidos en la solicitud");
            }            if (productoIds.size() != cantidades.size() || productoIds.size() != preciosUnitarios.size()) {
                throw new RuntimeException("Inconsistencia en los datos de productos");
            }

            // Validar que el nombre del cliente sea obligatorio
            if (nombreCliente == null || nombreCliente.trim().isEmpty()) {
                throw new RuntimeException("El nombre del cliente es obligatorio");
            }

            // Validar correos (opcional, pero si se proporciona debe ser válido)
            List<String> correosValidos = new ArrayList<>();
            if (correos != null && correos.length > 0) {
                for (String correo : correos) {
                    if (correo != null && !correo.trim().isEmpty()) {
                        // Validación básica de formato de correo
                        if (correo.trim().contains("@") && correo.trim().contains(".")) {
                            correosValidos.add(correo.trim());
                        }
                    }
                }
            }// Validar stock disponible y productos vencidos/agotados antes de procesar la venta
            for (int i = 0; i < productoIds.size(); i++) {
                Producto producto = productoRepository.findById(productoIds.get(i).intValue()).orElseThrow();
                int cantidadSolicitada = cantidades.get(i);
                
                // Validar que el producto no esté vencido
                if (producto.isVencido()) {
                    throw new RuntimeException("No se puede procesar la venta. El producto '" + producto.getNombre() + 
                                             "' está vencido (fecha de vencimiento: " + producto.getFechaVencimiento() + ")");
                }
                
                // Validar que el producto no esté agotado
                if (producto.isAgotado()) {
                    throw new RuntimeException("No se puede procesar la venta. El producto '" + producto.getNombre() + 
                                             "' está agotado (sin stock disponible)");
                }
                
                // Validar stock disponible
                if (producto.getStock() < cantidadSolicitada) {
                    throw new RuntimeException("Stock insuficiente para el producto '" + producto.getNombre() + 
                                             "'. Stock disponible: " + producto.getStock() + 
                                             ", cantidad solicitada: " + cantidadSolicitada);
                }
            }            // 1. Construir la venta
            Venta venta = new Venta();
            // Asignar correo principal si existe, sino dejar null
            venta.setCorreoCliente(correosValidos.isEmpty() ? null : correosValidos.get(0));
            venta.setNombreCliente(nombreCliente.trim());
            venta.setFecha(LocalDateTime.now());
            venta.setUsuarioCajero(usuario.getNombre()); // Asignar el usuario/cajero que registra la venta
            List<DetalleVenta> detalles = new ArrayList<>();
              for (int i = 0; i < productoIds.size(); i++) {
                Producto producto = productoRepository.findById(productoIds.get(i).intValue()).orElseThrow();
                int cantidad = cantidades.get(i);
                double precio = preciosUnitarios.get(i);
                DetalleVenta detalle = new DetalleVenta();
                detalle.setProducto(producto);
                detalle.setCantidad(cantidad);
                detalle.setPrecioUnitario(precio);
                // Bidireccionalidad: agrega el detalle a la venta
                detalle.setVenta(venta);
                detalles.add(detalle);
            }
            // Bidireccionalidad: asegura que cada detalle esté en la lista de la venta
            venta.setDetalles(detalles);
            // Calcula el total después de asociar detalles
            venta.setTotal(venta.getDetalles().stream().mapToDouble(DetalleVenta::getSubtotal).sum());            // 2. Guarda la venta (y los detalles por cascade)
            Venta ventaGuardada = ventaRepository.saveAndFlush(venta);

            // 2.1. Deducir stock y registrar movimientos en historial
            for (int i = 0; i < productoIds.size(); i++) {
                Producto producto = productoRepository.findById(productoIds.get(i).intValue()).orElseThrow();
                int cantidadVendida = cantidades.get(i);
                
                // Calcular nuevo stock
                Integer stockAnterior = producto.getStock();
                Integer nuevoStock = stockAnterior - cantidadVendida;
                
                // Actualizar stock del producto
                producto.setStock(nuevoStock);
                productoRepository.save(producto);
                  // Registrar movimiento en historial de stock
                String motivo = "Venta #" + ventaGuardada.getId();
                historialStockService.registrarMovimiento(
                    producto.getId(), stockAnterior, nuevoStock, 
                    "SALIDA", motivo, usuario.getNombre()
                );
                
                logger.info("Stock actualizado para producto {}: {} -> {} (venta de {} unidades)", 
                           producto.getNombre(), stockAnterior, nuevoStock, cantidadVendida);
            }            // 3. Recupera la venta con los detalles usando fetch join
            Venta ventaConDetalles = ventaRepository.findByIdWithDetalles(ventaGuardada.getId());

            // LOG: Verificar objeto venta recuperado
            logger.info("VentaConDetalles: id={}, fecha={}, correo={}, total={}",
                ventaConDetalles.getId(), ventaConDetalles.getFecha(), ventaConDetalles.getCorreoCliente(), ventaConDetalles.getTotal());
            if (ventaConDetalles.getDetalles() == null || ventaConDetalles.getDetalles().isEmpty()) {
                logger.error("La venta recuperada no tiene detalles. Venta ID: {}", ventaConDetalles.getId());
            } else {
                for (DetalleVenta d : ventaConDetalles.getDetalles()) {
                    logger.info("DetalleVenta: producto={}, cantidad={}, precioUnitario={}, subtotal={}",
                        d.getProducto() != null ? d.getProducto().getNombre() : null,
                        d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal());
                }
                logger.info("Venta recuperada con {} detalles. Venta ID: {}", ventaConDetalles.getDetalles().size(), ventaConDetalles.getId());
            }

            // DEBUG: Forzar seteo manual de datos si están nulos
            if (ventaConDetalles.getFecha() == null) ventaConDetalles.setFecha(venta.getFecha());
            if (ventaConDetalles.getCorreoCliente() == null) ventaConDetalles.setCorreoCliente(venta.getCorreoCliente());
            if (ventaConDetalles.getTotal() == 0) ventaConDetalles.setTotal(venta.getTotal());            // 4. Genera el PDF
            byte[] pdfFactura = PDFGenerator.generarFacturaPDF(ventaConDetalles);

            // 5. Envía el correo con el PDF adjunto solo si hay correos válidos
            if (!correosValidos.isEmpty()) {
                emailService.enviarFacturaConAdjunto(
                    correosValidos.toArray(new String[0]),
                    "Factura de su compra",
                    "¡Gracias por su compra! Adjuntamos los detalles de su factura.",
                    pdfFactura,
                    "factura.pdf"
                );
                redirectAttributes.addFlashAttribute("mensaje", "Venta registrada y factura enviada por correo.");
            } else {
                redirectAttributes.addFlashAttribute("mensaje", "Venta registrada exitosamente. No se proporcionó correo para envío de factura.");
            }
            return "redirect:/test-ventas?success=true";
        } catch (Exception e) {
            // En caso de error, recargar productos y categorías para el formulario
            List<Producto> productos = productoRepository.findByActivoTrueOrderByNombre();
            List<Categoria> categorias = categoriaRepository.findByActivoTrueOrderByNombre();
            model.addAttribute("productos", productos);
            model.addAttribute("categorias", categorias);
            model.addAttribute("error", e.getMessage());
            return "ventas";
        }
    }
}
