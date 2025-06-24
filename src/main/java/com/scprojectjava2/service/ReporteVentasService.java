package com.scprojectjava2.service;

import com.scprojectjava2.model.*;
import com.scprojectjava2.repository.*;
import com.scprojectjava2.utils.PDFGenerator;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReporteVentasService {

    @Autowired
    private VentaRepository ventaRepository;
    
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    public byte[] generarReportePDF(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin, int limite) throws Exception {
        switch (tipoReporte.toLowerCase()) {
            case "productos-mas-vendidos":
                return generarPDFProductosMasVendidos(limite);
            case "ventas-por-fecha":
                return generarPDFVentasPorFecha(fechaInicio, fechaFin);
            case "ingresos-por-producto":
                return generarPDFIngresosPorProducto(limite);
            case "resumen-general":
                return generarPDFResumenGeneral();
            default:
                throw new IllegalArgumentException("Tipo de reporte no válido: " + tipoReporte);
        }
    }

    public byte[] generarReporteExcel(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin, int limite) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte de Ventas");
            
            switch (tipoReporte.toLowerCase()) {
                case "productos-mas-vendidos":
                    generarExcelProductosMasVendidos(sheet, limite);
                    break;
                case "ventas-por-fecha":
                    generarExcelVentasPorFecha(sheet, fechaInicio, fechaFin);
                    break;
                case "ingresos-por-producto":
                    generarExcelIngresosPorProducto(sheet, limite);
                    break;
                case "resumen-general":
                    generarExcelResumenGeneral(sheet);
                    break;
                default:
                    throw new IllegalArgumentException("Tipo de reporte no válido: " + tipoReporte);
            }
            
            // Ajustar ancho de columnas
            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private byte[] generarPDFProductosMasVendidos(int limite) throws Exception {
        List<Object[]> productos = detalleVentaRepository.getProductosMasVendidos();
        
        StringBuilder content = new StringBuilder();
        content.append("<h1>Productos Más Vendidos</h1>");
        content.append("<p>Fecha de generación: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        content.append("<table border='1' style='width:100%; border-collapse: collapse;'>");
        content.append("<thead><tr><th>Posición</th><th>Producto</th><th>Cantidad Vendida</th></tr></thead>");
        content.append("<tbody>");
        
        for (int i = 0; i < Math.min(limite, productos.size()); i++) {
            Object[] row = productos.get(i);
            content.append("<tr>");
            content.append("<td>").append(i + 1).append("</td>");
            content.append("<td>").append(row[1]).append("</td>");
            content.append("<td>").append(row[2]).append("</td>");
            content.append("</tr>");
        }
        
        content.append("</tbody></table>");
        
        return PDFGenerator.generarPDFDesdeHTML(content.toString());
    }

    private byte[] generarPDFVentasPorFecha(LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        List<Venta> ventas = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
        
        StringBuilder content = new StringBuilder();
        content.append("<h1>Reporte de Ventas por Fecha</h1>");
        content.append("<p>Período: ").append(fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
               .append(" - ").append(fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
        content.append("<p>Total de ventas: ").append(ventas.size()).append("</p>");
        
        double totalIngresos = ventas.stream().mapToDouble(Venta::getTotal).sum();
        content.append("<p>Total de ingresos: $").append(String.format("%.2f", totalIngresos)).append("</p>");
          content.append("<table border='1' style='width:100%; border-collapse: collapse;'>");
        content.append("<thead><tr><th>ID</th><th>Fecha</th><th>Cliente</th><th>Cajero</th><th>Total</th></tr></thead>");
        content.append("<tbody>");
        
        for (Venta venta : ventas) {
            content.append("<tr>");
            content.append("<td>").append(venta.getId()).append("</td>");
            content.append("<td>").append(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</td>");
            content.append("<td>").append(venta.getCorreoCliente()).append("</td>");
            content.append("<td>").append(venta.getUsuarioCajero() != null ? venta.getUsuarioCajero() : "No especificado").append("</td>");
            content.append("<td>$").append(String.format("%.2f", venta.getTotal())).append("</td>");
            content.append("</tr>");
        }
        
        content.append("</tbody></table>");
        
        return PDFGenerator.generarPDFDesdeHTML(content.toString());
    }

    private byte[] generarPDFIngresosPorProducto(int limite) throws Exception {
        List<Object[]> productos = detalleVentaRepository.getIngresosByProducto();
        
        StringBuilder content = new StringBuilder();
        content.append("<h1>Ingresos por Producto</h1>");
        content.append("<p>Fecha de generación: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        content.append("<table border='1' style='width:100%; border-collapse: collapse;'>");
        content.append("<thead><tr><th>Posición</th><th>Producto</th><th>Ingresos Generados</th></tr></thead>");
        content.append("<tbody>");
        
        for (int i = 0; i < Math.min(limite, productos.size()); i++) {
            Object[] row = productos.get(i);
            content.append("<tr>");
            content.append("<td>").append(i + 1).append("</td>");
            content.append("<td>").append(row[1]).append("</td>");
            content.append("<td>$").append(String.format("%.2f", row[2])).append("</td>");
            content.append("</tr>");
        }
        
        content.append("</tbody></table>");
        
        return PDFGenerator.generarPDFDesdeHTML(content.toString());
    }

    private byte[] generarPDFResumenGeneral() throws Exception {
        long totalVentas = ventaRepository.count();
        double totalIngresos = ventaRepository.findAll().stream().mapToDouble(Venta::getTotal).sum();
        long totalProductos = productoRepository.count();
        
        // Ventas del mes actual
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(23, 59, 59);
        List<Venta> ventasDelMes = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicioMes, finMes);
        double ingresosDelMes = ventasDelMes.stream().mapToDouble(Venta::getTotal).sum();
        
        StringBuilder content = new StringBuilder();
        content.append("<h1>Resumen General de Ventas</h1>");
        content.append("<p>Fecha de generación: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("</p>");
        
        content.append("<h2>Estadísticas Generales</h2>");
        content.append("<ul>");
        content.append("<li>Total de ventas realizadas: ").append(totalVentas).append("</li>");
        content.append("<li>Total de ingresos: $").append(String.format("%.2f", totalIngresos)).append("</li>");
        content.append("<li>Total de productos en catálogo: ").append(totalProductos).append("</li>");
        content.append("<li>Promedio por venta: $").append(totalVentas > 0 ? String.format("%.2f", totalIngresos / totalVentas) : "0.00").append("</li>");
        content.append("</ul>");
        
        content.append("<h2>Estadísticas del Mes Actual</h2>");
        content.append("<ul>");
        content.append("<li>Ventas del mes: ").append(ventasDelMes.size()).append("</li>");
        content.append("<li>Ingresos del mes: $").append(String.format("%.2f", ingresosDelMes)).append("</li>");
        content.append("<li>Promedio diario del mes: $").append(String.format("%.2f", ingresosDelMes / LocalDate.now().getDayOfMonth())).append("</li>");
        content.append("</ul>");
        
        // Top 5 productos más vendidos
        List<Object[]> topProductos = detalleVentaRepository.getProductosMasVendidos();
        content.append("<h2>Top 5 Productos Más Vendidos</h2>");
        content.append("<table border='1' style='width:100%; border-collapse: collapse;'>");
        content.append("<thead><tr><th>Posición</th><th>Producto</th><th>Cantidad Vendida</th></tr></thead>");
        content.append("<tbody>");
        
        for (int i = 0; i < Math.min(5, topProductos.size()); i++) {
            Object[] row = topProductos.get(i);
            content.append("<tr>");
            content.append("<td>").append(i + 1).append("</td>");
            content.append("<td>").append(row[1]).append("</td>");
            content.append("<td>").append(row[2]).append("</td>");
            content.append("</tr>");
        }
        
        content.append("</tbody></table>");
        
        return PDFGenerator.generarPDFDesdeHTML(content.toString());
    }

    private void generarExcelProductosMasVendidos(Sheet sheet, int limite) {
        List<Object[]> productos = detalleVentaRepository.getProductosMasVendidos();
        
        // Crear estilos
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Posición");
        headerRow.createCell(1).setCellValue("ID Producto");
        headerRow.createCell(2).setCellValue("Nombre Producto");
        headerRow.createCell(3).setCellValue("Cantidad Vendida");
        
        // Aplicar estilo a encabezados
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Llenar datos
        for (int i = 0; i < Math.min(limite, productos.size()); i++) {
            Object[] row = productos.get(i);
            Row dataRow = sheet.createRow(i + 1);
            dataRow.createCell(0).setCellValue(i + 1);
            dataRow.createCell(1).setCellValue(row[0].toString());
            dataRow.createCell(2).setCellValue(row[1].toString());
            dataRow.createCell(3).setCellValue(Integer.parseInt(row[2].toString()));
        }
    }

    private void generarExcelVentasPorFecha(Sheet sheet, LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        List<Venta> ventas = ventaRepository.findByFechaBetweenOrderByFechaDesc(inicio, fin);
        
        // Crear estilos
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
          // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID Venta");
        headerRow.createCell(1).setCellValue("Fecha");
        headerRow.createCell(2).setCellValue("Cliente");
        headerRow.createCell(3).setCellValue("Cajero");
        headerRow.createCell(4).setCellValue("Total");
        
        // Aplicar estilo a encabezados
        for (int i = 0; i < 5; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Llenar datos
        for (int i = 0; i < ventas.size(); i++) {
            Venta venta = ventas.get(i);
            Row dataRow = sheet.createRow(i + 1);
            dataRow.createCell(0).setCellValue(venta.getId());
            dataRow.createCell(1).setCellValue(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            dataRow.createCell(2).setCellValue(venta.getCorreoCliente());
            dataRow.createCell(3).setCellValue(venta.getUsuarioCajero() != null ? venta.getUsuarioCajero() : "No especificado");
            dataRow.createCell(4).setCellValue(venta.getTotal());
        }
    }

    private void generarExcelIngresosPorProducto(Sheet sheet, int limite) {
        List<Object[]> productos = detalleVentaRepository.getIngresosByProducto();
        
        // Crear estilos
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // Crear encabezados
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Posición");
        headerRow.createCell(1).setCellValue("ID Producto");
        headerRow.createCell(2).setCellValue("Nombre Producto");
        headerRow.createCell(3).setCellValue("Ingresos Generados");
        
        // Aplicar estilo a encabezados
        for (int i = 0; i < 4; i++) {
            headerRow.getCell(i).setCellStyle(headerStyle);
        }
        
        // Llenar datos
        for (int i = 0; i < Math.min(limite, productos.size()); i++) {
            Object[] row = productos.get(i);
            Row dataRow = sheet.createRow(i + 1);
            dataRow.createCell(0).setCellValue(i + 1);
            dataRow.createCell(1).setCellValue(row[0].toString());
            dataRow.createCell(2).setCellValue(row[1].toString());
            dataRow.createCell(3).setCellValue(Double.parseDouble(row[2].toString()));
        }
    }

    private void generarExcelResumenGeneral(Sheet sheet) {
        // Crear estilos
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        // Obtener datos
        long totalVentas = ventaRepository.count();
        double totalIngresos = ventaRepository.findAll().stream().mapToDouble(Venta::getTotal).sum();
        long totalProductos = productoRepository.count();
        
        int rowNum = 0;
        
        // Título
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("RESUMEN GENERAL DE VENTAS");
        titleCell.setCellStyle(headerStyle);
        
        rowNum++; // Fila vacía
        
        // Estadísticas generales
        Row statsHeaderRow = sheet.createRow(rowNum++);
        Cell statsHeaderCell = statsHeaderRow.createCell(0);
        statsHeaderCell.setCellValue("ESTADÍSTICAS GENERALES");
        statsHeaderCell.setCellStyle(headerStyle);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de ventas:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalVentas);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de ingresos:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalIngresos);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de productos:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalProductos);
          sheet.createRow(rowNum++).createCell(0).setCellValue("Promedio por venta:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalVentas > 0 ? totalIngresos / totalVentas : 0);
    }
    
    // Métodos para reportes por usuario
    public byte[] generarReportePDFPorUsuario(String usuarioCajero, LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        List<Venta> ventas;
        
        if (fechaInicio != null && fechaFin != null) {
            LocalDateTime inicio = fechaInicio.atStartOfDay();
            LocalDateTime fin = fechaFin.atTime(23, 59, 59);
            ventas = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(usuarioCajero, inicio, fin);
        } else {
            ventas = ventaRepository.findByUsuarioCajeroOrderByFechaDesc(usuarioCajero);
        }
        
        // Estadísticas del usuario
        long totalVentas = ventas.size();
        double totalIngresos = ventas.stream().mapToDouble(Venta::getTotal).sum();
        double promedioVenta = totalVentas > 0 ? totalIngresos / totalVentas : 0;
        
        String titulo = "Reporte de Ventas por Usuario: " + usuarioCajero;
        String subtitulo = fechaInicio != null && fechaFin != null ? 
            "Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
            " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) :
            "Todas las ventas";
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDFGenerator.generarReporteVentasPorUsuario(baos, ventas, usuarioCajero, titulo, subtitulo, 
                totalVentas, totalIngresos, promedioVenta);
            return baos.toByteArray();
        }
    }
    
    public byte[] generarReporteExcelPorUsuario(String usuarioCajero, LocalDate fechaInicio, LocalDate fechaFin) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Reporte por Usuario");
            
            List<Venta> ventas;
            if (fechaInicio != null && fechaFin != null) {
                LocalDateTime inicio = fechaInicio.atStartOfDay();
                LocalDateTime fin = fechaFin.atTime(23, 59, 59);
                ventas = ventaRepository.findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(usuarioCajero, inicio, fin);
            } else {
                ventas = ventaRepository.findByUsuarioCajeroOrderByFechaDesc(usuarioCajero);
            }
            
            generarExcelVentasPorUsuario(sheet, ventas, usuarioCajero, fechaInicio, fechaFin);
            
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                return baos.toByteArray();
            }
        }
    }
    
    private void generarExcelVentasPorUsuario(Sheet sheet, List<Venta> ventas, String usuarioCajero, 
                                             LocalDate fechaInicio, LocalDate fechaFin) {
        
        // Estilos
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font headerFont = sheet.getWorkbook().createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        
        CellStyle titleStyle = sheet.getWorkbook().createCellStyle();
        Font titleFont = sheet.getWorkbook().createFont();
        titleFont.setBold(true);
        titleFont.setFontHeightInPoints((short) 16);
        titleStyle.setFont(titleFont);
        
        int rowNum = 0;
        
        // Título
        Cell titleCell = sheet.createRow(rowNum++).createCell(0);
        titleCell.setCellValue("REPORTE DE VENTAS POR USUARIO");
        titleCell.setCellStyle(titleStyle);
        
        // Usuario
        sheet.createRow(rowNum++).createCell(0).setCellValue("Usuario/Cajero: " + usuarioCajero);
        
        // Período
        if (fechaInicio != null && fechaFin != null) {
            sheet.createRow(rowNum++).createCell(0).setCellValue(
                "Período: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            sheet.createRow(rowNum++).createCell(0).setCellValue("Período: Todas las ventas");
        }
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Fecha de generación: " + 
            LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        rowNum++; // Espacio
        
        // Estadísticas
        long totalVentas = ventas.size();
        double totalIngresos = ventas.stream().mapToDouble(Venta::getTotal).sum();
        double promedioVenta = totalVentas > 0 ? totalIngresos / totalVentas : 0;
        
        Cell statsHeaderCell = sheet.createRow(rowNum++).createCell(0);
        statsHeaderCell.setCellValue("ESTADÍSTICAS DEL USUARIO");
        statsHeaderCell.setCellStyle(headerStyle);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de ventas:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalVentas);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Total de ingresos:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(totalIngresos);
        
        sheet.createRow(rowNum++).createCell(0).setCellValue("Promedio por venta:");
        sheet.getRow(rowNum-1).createCell(1).setCellValue(promedioVenta);
        
        rowNum++; // Espacio
        
        // Encabezados de la tabla
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"ID Venta", "Fecha", "Cliente", "Total", "Productos"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // Datos de ventas
        for (Venta venta : ventas) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(venta.getId());
            row.createCell(1).setCellValue(venta.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            row.createCell(2).setCellValue(venta.getCorreoCliente() != null ? venta.getCorreoCliente() : "N/A");
            row.createCell(3).setCellValue(venta.getTotal());
            row.createCell(4).setCellValue(venta.getDetalles() != null ? venta.getDetalles().size() : 0);
        }
          // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }    /**
     * Generar factura individual en PDF para una venta específica
     */
    public byte[] generarFacturaIndividualPDF(Venta venta) throws Exception {
        return PDFGenerator.generarFacturaPDF(venta);
    }
}
