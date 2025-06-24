package com.scprojectjava2.controller;

import com.scprojectjava2.model.Categoria;
import com.scprojectjava2.model.Usuario;
import com.scprojectjava2.service.CategoriaService;
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
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;    @GetMapping
    public String listarCategorias(@RequestParam(name = "accion", required = false) String accion,
                                 @RequestParam(name = "id", required = false) Integer id,
                                 @RequestParam(name = "nombre", required = false) String nombre,
                                 @RequestParam(name = "descripcion", required = false) String descripcion,
                                 Model model, HttpSession session) {
        
        // Obtener usuario de la sesión para verificación de roles en la vista
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        
        // Inicializar por defecto la variable viendoInactivos como false
        model.addAttribute("viendoInactivos", false);

        if (id != null && nombre != null && descripcion != null) {
            // Para edición mediante el botón "Editar"
            Categoria categoriaEditar = new Categoria();
            categoriaEditar.setId(id);
            categoriaEditar.setNombre(nombre);
            categoriaEditar.setDescripcion(descripcion);
            
            model.addAttribute("categoria", categoriaEditar);
        } else {
            // Para el formulario de nueva categoría
            model.addAttribute("categoria", new Categoria());
        }

        if ("verInactivos".equals(accion)) {
            List<Categoria> categoriasInactivas = categoriaService.listarInactivas();
            model.addAttribute("categorias", categoriasInactivas);
            model.addAttribute("viendoInactivos", true);
            return "categorias";
        }

        if ("reactivar".equals(accion) && id != null) {
            categoriaService.reactivar(id);
            return "redirect:/categorias?accion=verInactivos";
        }

        // Acción por defecto: listar activas
        List<Categoria> categorias = categoriaService.listarActivas();
        model.addAttribute("categorias", categorias);
        return "categorias";
    }

    @PostMapping("/guardar")
    public String guardarCategoria(@ModelAttribute Categoria categoria) {
        if (categoria.getId() != null && categoria.getId() > 0) {
            categoriaService.actualizar(categoria);
        } else {
            categoriaService.agregar(categoria);
        }
        return "redirect:/categorias";
    }

    @PostMapping("/eliminar")
    public String eliminarCategoria(@RequestParam("idEliminar") int idEliminar) {
        categoriaService.eliminar(idEliminar);
        return "redirect:/categorias";
    }    @GetMapping("/reporte")
    public void generarReportePDF(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String fechaActual = dateFormatter.format(new Date());
        
        String cabecera = "Content-Disposition";
        String valor = "attachment; filename=categorias_" + fechaActual + ".pdf";
        
        response.setHeader(cabecera, valor);
        
        List<Categoria> categorias = categoriaService.listarActivas();
          // Crear contenido HTML con estilo consistente
        StringBuilder htmlContent = new StringBuilder();
        htmlContent.append("<h1>Lista de Categorías</h1>");
        
        // Crear tabla HTML
        htmlContent.append("<table>");
        htmlContent.append("<thead>");
        htmlContent.append("<tr>");
        htmlContent.append("<th>ID</th>");
        htmlContent.append("<th>Nombre</th>");
        htmlContent.append("<th>Descripción</th>");
        htmlContent.append("</tr>");
        htmlContent.append("</thead>");
        htmlContent.append("<tbody>");
        
        for (Categoria categoria : categorias) {
            htmlContent.append("<tr>");
            htmlContent.append("<td>").append(categoria.getId()).append("</td>");
            htmlContent.append("<td>").append(categoria.getNombre()).append("</td>");
            htmlContent.append("<td>").append(categoria.getDescripcion()).append("</td>");
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
