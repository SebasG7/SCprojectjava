package com.scprojectjava2.controller;

import com.scprojectjava2.model.Unidad;
import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.service.UnidadService;
import com.scprojectjava2.utils.PDFGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/unidades")
public class UnidadController {

    @Autowired
    private UnidadService unidadService;    @GetMapping
    public String listarUnidades(@RequestParam(name = "accion", required = false) String accion,
                                 @RequestParam(name = "id", required = false) Integer id,
                                 Model model, HttpSession session) {
        
        // Obtener usuario de la sesión para verificación de roles en la vista
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        
        // Inicializar por defecto la variable viendoInactivos como false
        model.addAttribute("viendoInactivos", false);        if ("editar".equals(accion) && id != null) {
            Unidad unidadEditar = unidadService.obtenerPorId(id);
            model.addAttribute("unidadEditar", unidadEditar);
            List<Unidad> unidades = unidadService.listarActivas();
            model.addAttribute("unidades", unidades);
            return "unidades"; // Thymeleaf: unidades.html
        }

        if ("verInactivos".equals(accion)) {
            List<Unidad> unidadesInactivas = unidadService.listarInactivos();
            model.addAttribute("unidades", unidadesInactivas);
            model.addAttribute("viendoInactivos", true);
            return "unidades"; // Utilizamos la misma plantilla pero con datos diferentes
        }

        if ("reactivar".equals(accion) && id != null) {
            unidadService.reactivar(id);
            return "redirect:/unidades?accion=verInactivos";
        }

        // Acción por defecto: listar activos
        List<Unidad> unidades = unidadService.listarActivas();
        model.addAttribute("unidades", unidades);
        return "unidades"; // Thymeleaf: unidades.html
    }@PostMapping
    public String guardarUnidad(@RequestParam(name = "id", required = false) Integer id,
                                 @RequestParam("nombre") String nombre,
                                 @RequestParam("abreviatura") String abreviatura) {
        Unidad unidad = new Unidad();
        unidad.setNombre(nombre);
        unidad.setAbreviatura(abreviatura);

        if (id != null && id > 0) {
            unidad.setId(id);
            unidadService.actualizar(unidad);
        } else {
            unidadService.agregar(unidad);
        }

        return "redirect:/unidades";
    }

    @PostMapping("/eliminar")
    public String eliminarUnidad(@RequestParam("id") int id) {
        unidadService.eliminar(id);
        return "redirect:/unidades";
    }    @GetMapping("/reporte")
    public void generarReportePDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = dateFormatter.format(new Date());
        
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=unidades_" + fechaActual + ".pdf";
        
        response.setHeader(cabecera, valor);
        
        List<Unidad> unidades = unidadService.listar();
          // Crear contenido HTML con estilo consistente
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<h1>Lista de Unidades</h1>");
        
        // Crear tabla HTML
        htmlContent.append("<table>");
        htmlContent.append("<thead>");
        htmlContent.append("<tr>");
        htmlContent.append("<th>ID</th>");
        htmlContent.append("<th>Nombre</th>");
        htmlContent.append("<th>Abreviatura</th>");
        htmlContent.append("</tr>");
        htmlContent.append("</thead>");
        htmlContent.append("<tbody>");
        
        for (Unidad unidad : unidades) {
            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(unidad.getId()).append("</td>");
            htmlContent.append("<td>").append(unidad.getNombre()).append("</td>");
            htmlContent.append("<td>").append(unidad.getAbreviatura()).append("</td>");
            htmlContent.append("</tr>");
        }
        
        htmlContent.append("</tbody>");
        htmlContent.append("</table>");
        
        // Generar PDF usando PDFGenerator
        byte[] pdfBytes = PDFGenerator.generarPDFDesdeHTML(htmlContent.toString());
        
        if (pdfBytes != null) {
            response.getOutputStream().write(pdfBytes);
        }
    }
}
