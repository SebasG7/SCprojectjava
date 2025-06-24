package com.scprojectjava2.controller;

import com.scprojectjava2.model.Producto;
import com.scprojectjava2.model.Venta;
import com.scprojectjava2.repository.ProductoRepository;
import com.scprojectjava2.repository.VentaRepository;
import com.scprojectjava2.service.EmailService;
import com.scprojectjava2.utils.PDFGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/ventas")
public class VentaController {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private VentaRepository ventaRepository;

    @GetMapping
    public String mostrarFormularioVentas(Model model) {
        try {
            List<Producto> productos = productoRepository.findAll();
            model.addAttribute("productos", productos);
            return "ventas";
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar los productos: " + e.getMessage());
            return "ventas";
        }
    }    @PostMapping
    public String procesarVenta(
            @RequestParam("correos") String[] correos,
            @RequestParam(value = "productos", required = false) List<String> productosStr,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpSession session) {
        try {
            // Obtener usuario de la sesión
            com.scprojectjava2.model.Usuario usuario = (com.scprojectjava2.model.Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                redirectAttributes.addFlashAttribute("error", "Usuario no autenticado");
                return "redirect:/login";
            }
            
            // Validar que se hayan seleccionado productos (esta lógica necesita ser implementada según tu estructura)
            // TODO: Implementar validación de productos seleccionados y sus cantidades
            // Por ejemplo, validar que no estén vencidos:
            // for (cada producto en la venta) {
            //     Producto producto = productoRepository.findById(productoId).orElseThrow();
            //     if (producto.isVencido()) {
            //         throw new RuntimeException("No se puede procesar la venta. El producto '" + producto.getNombre() + 
            //                                  "' está vencido (fecha de vencimiento: " + producto.getFechaVencimiento() + ")");
            //     }
            // }
            
            // 1. Construir y guardar la venta y sus detalles
            // Ejemplo: Venta venta = new Venta(...); // Completa con los datos recibidos
            Venta venta = new Venta();
            venta.setUsuarioCajero(usuario.getNombre()); // Asignar el usuario/cajero que registra la venta
            // ...asigna los campos de la venta y sus detalles aquí...
            Venta ventaGuardada = ventaRepository.save(venta);

            // 2. Recuperar la venta guardada con los detalles (para asegurar que están cargados)
            Venta ventaConDetalles = ventaRepository.findById(ventaGuardada.getId()).orElseThrow();

            // 3. Generar el PDF de la factura
            byte[] pdfFactura = PDFGenerator.generarFacturaPDF(ventaConDetalles);

            // 4. Enviar el correo con la factura adjunta (un solo correo por destinatario)
            emailService.enviarFacturaConAdjunto(
                    correos,
                    "Factura de su compra",
                    "¡Gracias por su compra! Adjuntamos los detalles de su factura.",
                    pdfFactura,
                    "factura.pdf"
            );

            redirectAttributes.addFlashAttribute("mensaje", "Venta registrada y factura enviada.");
            return "redirect:/ventas?success=true";
        } catch (Exception e) {
            model.addAttribute("error", "Error al registrar la venta y enviar el correo: " + e.getMessage());
            return "ventas";
        }
    }
}