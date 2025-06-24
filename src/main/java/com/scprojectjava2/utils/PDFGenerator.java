package com.scprojectjava2.utils;

import com.scprojectjava2.model.DetalleVenta;
import com.scprojectjava2.model.Venta;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.util.List;

public class PDFGenerator {
    // Modern color palette for professional design
    private static final BaseColor COLOR_PRIMARY = new BaseColor(37, 99, 235); // Blue-600
    private static final BaseColor COLOR_SECONDARY = new BaseColor(71, 85, 105); // Slate-600
    private static final BaseColor COLOR_LIGHT_GRAY = new BaseColor(248, 250, 252); // Slate-50
    private static final BaseColor COLOR_MEDIUM_GRAY = new BaseColor(226, 232, 240); // Slate-200
    private static final BaseColor COLOR_DARK_GRAY = new BaseColor(51, 65, 85); // Slate-700
    private static final BaseColor COLOR_SUCCESS = new BaseColor(34, 197, 94); // Green-500

    public static byte[] generarFacturaPDF(Venta venta) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(documento, baos);
            documento.open();

            // Enhanced font styles
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, COLOR_PRIMARY);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_SECONDARY);
            Font textoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_DARK_GRAY);
            Font textoNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_DARK_GRAY);
            Font textoMeta = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_SECONDARY);

            // Header with modern styling
            addHeader(documento, venta, tituloFont, subtituloFont, textoNormal, textoMeta);
            
            // Main content table with professional design
            addInvoiceTable(documento, venta, textoNegrita, textoNormal);
            
            // Footer with professional touch
            addFooter(documento, textoMeta);

            documento.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addHeader(Document documento, Venta venta, Font tituloFont, Font subtituloFont, Font textoNormal, Font textoMeta) throws DocumentException {
        // Company header section
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2, 1});
        headerTable.setSpacingAfter(25);

        // Left side - Company info
        PdfPCell companyCell = new PdfPCell();
        companyCell.setBorder(Rectangle.NO_BORDER);
        companyCell.setPaddingBottom(10);
        
        Paragraph companyName = new Paragraph("SISTEMA DE VENTAS", tituloFont);
        companyName.setSpacingAfter(5);
        companyCell.addElement(companyName);
        
        Paragraph companySubtitle = new Paragraph("Sistema de Control de Proyectos", subtituloFont);
        companySubtitle.setSpacingAfter(8);
        companyCell.addElement(companySubtitle);
        
        companyCell.addElement(new Paragraph("Gestión profesional de ventas e inventario", textoMeta));
        
        headerTable.addCell(companyCell);

        // Right side - Invoice info with colored background
        PdfPCell invoiceInfoCell = new PdfPCell();
        invoiceInfoCell.setBackgroundColor(COLOR_LIGHT_GRAY);
        invoiceInfoCell.setPadding(15);
        invoiceInfoCell.setBorderColor(COLOR_MEDIUM_GRAY);
        invoiceInfoCell.setBorderWidth(1);
        
        Paragraph invoiceTitle = new Paragraph("FACTURA DE VENTA", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, COLOR_PRIMARY));
        invoiceTitle.setAlignment(Element.ALIGN_CENTER);
        invoiceTitle.setSpacingAfter(8);
        invoiceInfoCell.addElement(invoiceTitle);
        
        invoiceInfoCell.addElement(new Paragraph("ID: #" + venta.getId(), textoNormal));
        invoiceInfoCell.addElement(new Paragraph("Fecha: " + venta.getFecha().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), textoNormal));
        
        headerTable.addCell(invoiceInfoCell);
        documento.add(headerTable);

        // Customer information section
        PdfPTable customerTable = new PdfPTable(1);
        customerTable.setWidthPercentage(100);
        customerTable.setSpacingAfter(20);
        
        PdfPCell customerCell = new PdfPCell();
        customerCell.setBackgroundColor(new BaseColor(249, 250, 251));
        customerCell.setPadding(12);
        customerCell.setBorderColor(COLOR_MEDIUM_GRAY);
          Paragraph customerTitle = new Paragraph("INFORMACIÓN DEL CLIENTE", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, COLOR_SECONDARY));
        customerTitle.setSpacingAfter(5);
        customerCell.addElement(customerTitle);
        
        // Información del cliente con prioridad al nombre
        if (venta.getNombreCliente() != null && !venta.getNombreCliente().trim().isEmpty()) {
            customerCell.addElement(new Paragraph("Cliente: " + venta.getNombreCliente(), textoNormal));
            if (venta.getCorreoCliente() != null && !venta.getCorreoCliente().trim().isEmpty()) {
                customerCell.addElement(new Paragraph("Correo: " + venta.getCorreoCliente(), textoNormal));
            }
        } else if (venta.getCorreoCliente() != null && !venta.getCorreoCliente().trim().isEmpty()) {
            customerCell.addElement(new Paragraph("Cliente: " + venta.getCorreoCliente(), textoNormal));
        } else {
            customerCell.addElement(new Paragraph("Cliente: Cliente general", textoNormal));
        }
        
        if (venta.getUsuarioCajero() != null) {
            customerCell.addElement(new Paragraph("Atendido por: " + venta.getUsuarioCajero(), textoNormal));
        }
        
        customerTable.addCell(customerCell);
        documento.add(customerTable);
    }

    private static void addInvoiceTable(Document documento, Venta venta, Font textoNegrita, Font textoNormal) throws DocumentException {
        // Products table with modern design
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{4, 1.5f, 2, 2});
        tabla.setSpacingAfter(15);

        // Enhanced table headers
        String[] headers = {"Producto", "Cant.", "Precio Unit.", "Subtotal"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(12);
            cell.setBorderWidth(0);
            tabla.addCell(cell);
        }

        // Table rows with alternating colors
        boolean isAlternate = false;
        for (DetalleVenta d : venta.getDetalles()) {
            BaseColor rowColor = isAlternate ? COLOR_LIGHT_GRAY : BaseColor.WHITE;
            
            // Product name
            PdfPCell productCell = new PdfPCell(new Phrase(d.getProducto().getNombre(), textoNormal));
            productCell.setBackgroundColor(rowColor);
            productCell.setPadding(10);
            productCell.setBorderColor(COLOR_MEDIUM_GRAY);
            productCell.setBorderWidth(0.5f);
            tabla.addCell(productCell);
            
            // Quantity
            PdfPCell quantityCell = new PdfPCell(new Phrase(String.valueOf(d.getCantidad()), textoNormal));
            quantityCell.setBackgroundColor(rowColor);
            quantityCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            quantityCell.setPadding(10);
            quantityCell.setBorderColor(COLOR_MEDIUM_GRAY);
            quantityCell.setBorderWidth(0.5f);
            tabla.addCell(quantityCell);
            
            // Unit price
            PdfPCell priceCell = new PdfPCell(new Phrase("$" + String.format("%.2f", d.getPrecioUnitario()), textoNormal));
            priceCell.setBackgroundColor(rowColor);
            priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            priceCell.setPadding(10);
            priceCell.setBorderColor(COLOR_MEDIUM_GRAY);
            priceCell.setBorderWidth(0.5f);
            tabla.addCell(priceCell);
            
            // Subtotal
            PdfPCell subtotalCell = new PdfPCell(new Phrase("$" + String.format("%.2f", d.getSubtotal()), textoNegrita));
            subtotalCell.setBackgroundColor(rowColor);
            subtotalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            subtotalCell.setPadding(10);
            subtotalCell.setBorderColor(COLOR_MEDIUM_GRAY);
            subtotalCell.setBorderWidth(0.5f);
            tabla.addCell(subtotalCell);
            
            isAlternate = !isAlternate;
        }

        // Total row with enhanced styling
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("TOTAL", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE)));
        totalLabelCell.setColspan(3);
        totalLabelCell.setBackgroundColor(COLOR_SUCCESS);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPadding(12);
        totalLabelCell.setBorderWidth(0);
        tabla.addCell(totalLabelCell);

        PdfPCell totalValueCell = new PdfPCell(new Phrase("$" + String.format("%.2f", venta.getTotal()), 
            new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.WHITE)));
        totalValueCell.setBackgroundColor(COLOR_SUCCESS);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPadding(12);
        totalValueCell.setBorderWidth(0);
        tabla.addCell(totalValueCell);

        documento.add(tabla);
    }

    private static void addFooter(Document documento, Font textoMeta) throws DocumentException {
        // Professional footer
        documento.add(new Paragraph("\n"));
        
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);
        
        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorder(Rectangle.TOP);
        footerCell.setBorderColor(COLOR_MEDIUM_GRAY);
        footerCell.setPaddingTop(15);
        footerCell.setPaddingBottom(10);
        
        Paragraph footerText = new Paragraph("Gracias por su compra. Este documento es generado automáticamente por nuestro sistema.", textoMeta);
        footerText.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(footerText);
        
        Paragraph timestamp = new Paragraph("Documento generado el " + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' HH:mm")), textoMeta);
        timestamp.setAlignment(Element.ALIGN_CENTER);
        footerCell.addElement(timestamp);
        
        footerTable.addCell(footerCell);
        documento.add(footerTable);
    }    public static byte[] generarPDFDesdeHTML(String htmlContent) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(documento, baos);
            documento.open();            // Enhanced font configurations
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_PRIMARY);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_SECONDARY);
            Font textoNormal = FontFactory.getFont(FontFactory.HELVETICA, 11, COLOR_DARK_GRAY);
            Font textoMeta = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_SECONDARY);

            // Professional header
            addReportHeader(documento, htmlContent, tituloFont, textoMeta);

            // Process content
            processReportContent(documento, htmlContent, textoNormal, subtituloFont);            // Process table if exists
            if (htmlContent.contains("<table")) {
                procesarTablaHTMLMejorada(documento, htmlContent);
            }

            documento.close();
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void addReportHeader(Document documento, String htmlContent, Font tituloFont, Font textoMeta) throws DocumentException {
        // Extract and display title with professional styling
        String titulo = "";
        if (htmlContent.contains("<h1>")) {
            titulo = htmlContent.substring(
                htmlContent.indexOf("<h1>") + 4,
                htmlContent.indexOf("</h1>")
            );
        }

        if (!titulo.isEmpty()) {
            // Main title with modern styling
            Paragraph tituloParrafo = new Paragraph(titulo, tituloFont);
            tituloParrafo.setAlignment(Element.ALIGN_CENTER);
            tituloParrafo.setSpacingAfter(15);
            documento.add(tituloParrafo);

            // Professional separator line
            PdfPTable separatorTable = new PdfPTable(1);
            separatorTable.setWidthPercentage(100);
            PdfPCell separatorCell = new PdfPCell();
            separatorCell.setFixedHeight(2);
            separatorCell.setBackgroundColor(COLOR_PRIMARY);
            separatorCell.setBorder(Rectangle.NO_BORDER);
            separatorTable.addCell(separatorCell);
            separatorTable.setSpacingAfter(20);
            documento.add(separatorTable);
        }

        // Timestamp with professional formatting
        Paragraph fechaGeneracion = new Paragraph(
            "Documento generado el " + 
            java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy 'a las' HH:mm")),
            textoMeta
        );
        fechaGeneracion.setAlignment(Element.ALIGN_RIGHT);
        fechaGeneracion.setSpacingAfter(20);
        documento.add(fechaGeneracion);
    }

    private static void processReportContent(Document documento, String htmlContent, Font textoNormal, Font subtituloFont) throws DocumentException {
        // Process content sections (excluding tables)
        if (htmlContent.contains("<p>") && !htmlContent.substring(0, htmlContent.indexOf("<p>")).contains("<table")) {
            String contenidoSinTabla = htmlContent;
            if (htmlContent.contains("<table")) {
                contenidoSinTabla = htmlContent.substring(0, htmlContent.indexOf("<table"));
            }
            
            // Process sections and lists
            String[] secciones = contenidoSinTabla.split("</p>");
            for (String seccion : secciones) {
                if (seccion.contains("<h2>")) {
                    String subtitulo = seccion.substring(seccion.indexOf("<h2>") + 4, seccion.indexOf("</h2>"));
                    Paragraph subtituloP = new Paragraph(subtitulo, subtituloFont);
                    subtituloP.setSpacingBefore(15);
                    subtituloP.setSpacingAfter(10);
                    documento.add(subtituloP);
                } else if (seccion.contains("<p>")) {
                    String texto = seccion.substring(seccion.indexOf("<p>") + 3);
                    texto = texto.replaceAll("<[^>]*>", "").trim();
                    if (!texto.isEmpty()) {
                        Paragraph parrafo = new Paragraph(texto, textoNormal);
                        parrafo.setSpacingAfter(8);
                        documento.add(parrafo);
                    }
                }
            }
        }
    }

    private static void procesarTablaHTMLMejorada(Document documento, String htmlContent) throws DocumentException {
        try {
            // Extract table content
            String tablaContent = htmlContent.substring(
                htmlContent.indexOf("<table"), 
                htmlContent.indexOf("</table>") + 8
            );

            // Count columns
            int numColumnas = 0;
            if (tablaContent.contains("<thead>") || tablaContent.contains("<th>")) {
                String headerSection = tablaContent;
                if (tablaContent.contains("<thead>")) {
                    headerSection = tablaContent.substring(
                        tablaContent.indexOf("<thead>"),
                        tablaContent.indexOf("</thead>") + 8
                    );
                }
                
                String tempHeader = headerSection;
                while (tempHeader.contains("<th>")) {
                    numColumnas++;
                    tempHeader = tempHeader.substring(tempHeader.indexOf("<th>") + 4);
                }
            }

            if (numColumnas == 0 && tablaContent.contains("<td>")) {
                String primeraFila = tablaContent.substring(
                    tablaContent.indexOf("<tr>"),
                    tablaContent.indexOf("</tr>") + 5
                );
                String tempFila = primeraFila;
                while (tempFila.contains("<td>")) {
                    numColumnas++;
                    tempFila = tempFila.substring(tempFila.indexOf("<td>") + 4);
                }
            }

            if (numColumnas > 0) {
                PdfPTable tabla = new PdfPTable(numColumnas);
                tabla.setWidthPercentage(100);
                tabla.setSpacingBefore(20f);
                tabla.setSpacingAfter(15f);

                // Process table rows with enhanced styling
                String[] filas = tablaContent.split("<tr>");
                boolean procesoEncabezados = false;
                boolean isAlternate = false;

                for (String fila : filas) {
                    if (!fila.contains("<th>") && !fila.contains("<td>")) continue;

                    // Process headers with modern styling
                    if (fila.contains("<th>") && !procesoEncabezados) {
                        String[] encabezados = fila.split("<th>|</th>");
                        for (String encabezado : encabezados) {
                            encabezado = encabezado.trim().replaceAll("<[^>]*>", "");
                            if (!encabezado.isEmpty()) {
                                PdfPCell cell = new PdfPCell(new Phrase(encabezado, 
                                    new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
                                cell.setBackgroundColor(COLOR_PRIMARY);
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                cell.setPadding(12);
                                cell.setBorderWidth(0);
                                tabla.addCell(cell);
                            }
                        }
                        procesoEncabezados = true;
                    }
                    // Process data cells with alternating colors
                    else if (fila.contains("<td>")) {
                        BaseColor rowColor = isAlternate ? COLOR_LIGHT_GRAY : BaseColor.WHITE;
                        String[] celdas = fila.split("<td>|</td>");
                        for (String celda : celdas) {
                            celda = celda.trim().replaceAll("<[^>]*>", "");
                            if (!celda.isEmpty()) {
                                PdfPCell cell = new PdfPCell(new Phrase(celda, 
                                    FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_DARK_GRAY)));
                                cell.setBackgroundColor(rowColor);
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                                cell.setPadding(8);
                                cell.setBorderColor(COLOR_MEDIUM_GRAY);
                                cell.setBorderWidth(0.5f);
                                tabla.addCell(cell);
                            }
                        }
                        isAlternate = !isAlternate;
                    }
                }

                documento.add(tabla);
            }
        } catch (Exception e) {
            // Fallback to simple message if table processing fails
            documento.add(new Paragraph("Datos de la tabla no disponibles", 
                FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_SECONDARY)));
        }    }
    
    public static void generarReporteVentasPorUsuario(ByteArrayOutputStream baos, List<Venta> ventas, 
                                                     String usuarioCajero, String titulo, String subtitulo,
                                                     long totalVentas, double totalIngresos, double promedioVenta) {
        try {
            Document documento = new Document(PageSize.A4, 50, 50, 60, 50);
            PdfWriter.getInstance(documento, baos);
            documento.open();
              
            // Enhanced font styles for professional appearance
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, COLOR_PRIMARY);
            Font subtituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, COLOR_SECONDARY);
            Font textoNegrita = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, COLOR_DARK_GRAY);
            Font textoNormal = FontFactory.getFont(FontFactory.HELVETICA, 10, COLOR_DARK_GRAY);
            Font textoMeta = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_SECONDARY);
            
            // Professional header section
            addUserReportHeader(documento, titulo, subtitulo, usuarioCajero, tituloFont, subtituloFont, textoNegrita, textoMeta);
            
            // Statistics section with enhanced design
            addUserStatistics(documento, totalVentas, totalIngresos, promedioVenta, textoNegrita, textoNormal);
            
            // Sales details table
            if (!ventas.isEmpty()) {
                addUserSalesTable(documento, ventas, textoNegrita, textoNormal);
            } else {
                addNoDataMessage(documento, textoNormal);
            }
            
            documento.close();
            
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el PDF: " + e.getMessage(), e);
        }
    }

    private static void addUserReportHeader(Document documento, String titulo, String subtitulo, String usuarioCajero, 
                                          Font tituloFont, Font subtituloFont, Font textoNegrita, Font textoMeta) throws DocumentException {
        // Main title with professional styling
        Paragraph tituloP = new Paragraph(titulo, tituloFont);
        tituloP.setAlignment(Element.ALIGN_CENTER);
        tituloP.setSpacingAfter(10);
        documento.add(tituloP);
        
        // Subtitle with period information
        Paragraph subtituloP = new Paragraph(subtitulo, subtituloFont);
        subtituloP.setAlignment(Element.ALIGN_CENTER);
        subtituloP.setSpacingAfter(25);
        documento.add(subtituloP);

        // Professional separator line
        PdfPTable separatorTable = new PdfPTable(1);
        separatorTable.setWidthPercentage(100);
        PdfPCell separatorCell = new PdfPCell();
        separatorCell.setFixedHeight(2);
        separatorCell.setBackgroundColor(COLOR_PRIMARY);
        separatorCell.setBorder(Rectangle.NO_BORDER);
        separatorTable.addCell(separatorCell);
        separatorTable.setSpacingAfter(20);
        documento.add(separatorTable);
        
        // User and generation info in a styled table
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 1});
        
        // Left side - User info
        PdfPCell userInfoCell = new PdfPCell();
        userInfoCell.setBorder(Rectangle.NO_BORDER);
        userInfoCell.addElement(new Paragraph("Usuario/Cajero: " + usuarioCajero, textoNegrita));
        infoTable.addCell(userInfoCell);
        
        // Right side - Generation date
        PdfPCell dateCell = new PdfPCell();
        dateCell.setBorder(Rectangle.NO_BORDER);
        Paragraph dateP = new Paragraph("Fecha de generación: " + 
            java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")), textoMeta);
        dateP.setAlignment(Element.ALIGN_RIGHT);
        dateCell.addElement(dateP);
        infoTable.addCell(dateCell);
        
        infoTable.setSpacingAfter(25);
        documento.add(infoTable);
    }

    private static void addUserStatistics(Document documento, long totalVentas, double totalIngresos, double promedioVenta,
                                        Font textoNegrita, Font textoNormal) throws DocumentException {
        // Statistics section title
        Paragraph estadisticasTitle = new Paragraph("ESTADÍSTICAS DEL USUARIO", textoNegrita);
        estadisticasTitle.setSpacingBefore(10);
        estadisticasTitle.setSpacingAfter(15);
        documento.add(estadisticasTitle);
        
        // Enhanced statistics table
        PdfPTable estadisticasTable = new PdfPTable(2);
        estadisticasTable.setWidthPercentage(70);
        estadisticasTable.setWidths(new float[]{3, 2});
        estadisticasTable.setSpacingAfter(25);
        
        // Modern table headers
        PdfPCell headerCell1 = new PdfPCell(new Phrase("Concepto", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
        headerCell1.setBackgroundColor(COLOR_PRIMARY);
        headerCell1.setPadding(12);
        headerCell1.setBorderWidth(0);
        estadisticasTable.addCell(headerCell1);
        
        PdfPCell headerCell2 = new PdfPCell(new Phrase("Valor", new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
        headerCell2.setBackgroundColor(COLOR_PRIMARY);
        headerCell2.setPadding(12);
        headerCell2.setBorderWidth(0);
        estadisticasTable.addCell(headerCell2);
        
        // Statistics data with alternating row colors
        boolean isAlternate = false;
        
        // Total sales
        BaseColor rowColor1 = isAlternate ? COLOR_LIGHT_GRAY : BaseColor.WHITE;
        PdfPCell cell1 = new PdfPCell(new Phrase("Total de ventas", textoNormal));
        cell1.setBackgroundColor(rowColor1);
        cell1.setPadding(10);
        cell1.setBorderColor(COLOR_MEDIUM_GRAY);
        cell1.setBorderWidth(0.5f);
        estadisticasTable.addCell(cell1);
        
        PdfPCell value1 = new PdfPCell(new Phrase(String.valueOf(totalVentas), textoNormal));
        value1.setBackgroundColor(rowColor1);
        value1.setPadding(10);
        value1.setBorderColor(COLOR_MEDIUM_GRAY);
        value1.setBorderWidth(0.5f);
        value1.setHorizontalAlignment(Element.ALIGN_CENTER);
        estadisticasTable.addCell(value1);
        
        isAlternate = !isAlternate;
        
        // Total income
        BaseColor rowColor2 = isAlternate ? COLOR_LIGHT_GRAY : BaseColor.WHITE;
        PdfPCell cell2 = new PdfPCell(new Phrase("Ingresos totales", textoNormal));
        cell2.setBackgroundColor(rowColor2);
        cell2.setPadding(10);
        cell2.setBorderColor(COLOR_MEDIUM_GRAY);
        cell2.setBorderWidth(0.5f);
        estadisticasTable.addCell(cell2);
        
        PdfPCell value2 = new PdfPCell(new Phrase(String.format("$%.2f", totalIngresos), textoNormal));
        value2.setBackgroundColor(rowColor2);
        value2.setPadding(10);
        value2.setBorderColor(COLOR_MEDIUM_GRAY);
        value2.setBorderWidth(0.5f);
        value2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        estadisticasTable.addCell(value2);
        
        isAlternate = !isAlternate;
        
        // Average per sale
        BaseColor rowColor3 = isAlternate ? COLOR_LIGHT_GRAY : BaseColor.WHITE;
        PdfPCell cell3 = new PdfPCell(new Phrase("Promedio por venta", textoNormal));
        cell3.setBackgroundColor(rowColor3);
        cell3.setPadding(10);
        cell3.setBorderColor(COLOR_MEDIUM_GRAY);
        cell3.setBorderWidth(0.5f);
        estadisticasTable.addCell(cell3);
        
        PdfPCell value3 = new PdfPCell(new Phrase(String.format("$%.2f", promedioVenta), textoNormal));
        value3.setBackgroundColor(rowColor3);
        value3.setPadding(10);
        value3.setBorderColor(COLOR_MEDIUM_GRAY);
        value3.setBorderWidth(0.5f);
        value3.setHorizontalAlignment(Element.ALIGN_RIGHT);
        estadisticasTable.addCell(value3);
        
        documento.add(estadisticasTable);
    }

    private static void addUserSalesTable(Document documento, List<Venta> ventas, Font textoNegrita, Font textoNormal) throws DocumentException {
        // Sales details section
        Paragraph ventasTitle = new Paragraph("DETALLE DE VENTAS", textoNegrita);
        ventasTitle.setSpacingBefore(20);
        ventasTitle.setSpacingAfter(15);
        documento.add(ventasTitle);
        
        // Enhanced sales table
        PdfPTable ventasTable = new PdfPTable(5);
        ventasTable.setWidthPercentage(100);
        ventasTable.setWidths(new float[]{1, 3, 3, 2, 1});
        ventasTable.setSpacingAfter(10);
        
        // Professional table headers
        String[] headers = {"ID", "Fecha", "Cliente", "Total", "Items"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, BaseColor.WHITE)));
            cell.setBackgroundColor(COLOR_PRIMARY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(12);
            cell.setBorderWidth(0);
            ventasTable.addCell(cell);
        }
        
        // Sales data with alternating colors
        boolean isAlternate = false;
        for (Venta venta : ventas) {
            BaseColor rowColor = isAlternate ? COLOR_LIGHT_GRAY : BaseColor.WHITE;
            
            // ID
            PdfPCell idCell = new PdfPCell(new Phrase(String.valueOf(venta.getId()), textoNormal));
            idCell.setBackgroundColor(rowColor);
            idCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            idCell.setPadding(8);
            idCell.setBorderColor(COLOR_MEDIUM_GRAY);
            idCell.setBorderWidth(0.5f);
            ventasTable.addCell(idCell);
            
            // Date
            String fechaFormateada = venta.getFecha().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            PdfPCell dateCell = new PdfPCell(new Phrase(fechaFormateada, textoNormal));
            dateCell.setBackgroundColor(rowColor);
            dateCell.setPadding(8);
            dateCell.setBorderColor(COLOR_MEDIUM_GRAY);
            dateCell.setBorderWidth(0.5f);
            ventasTable.addCell(dateCell);
            
            // Customer
            String cliente = venta.getCorreoCliente() != null ? venta.getCorreoCliente() : "N/A";
            PdfPCell customerCell = new PdfPCell(new Phrase(cliente, textoNormal));
            customerCell.setBackgroundColor(rowColor);
            customerCell.setPadding(8);
            customerCell.setBorderColor(COLOR_MEDIUM_GRAY);
            customerCell.setBorderWidth(0.5f);
            ventasTable.addCell(customerCell);
            
            // Total
            PdfPCell totalCell = new PdfPCell(new Phrase(String.format("$%.2f", venta.getTotal()), textoNormal));
            totalCell.setBackgroundColor(rowColor);
            totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalCell.setPadding(8);
            totalCell.setBorderColor(COLOR_MEDIUM_GRAY);
            totalCell.setBorderWidth(0.5f);
            ventasTable.addCell(totalCell);
            
            // Items count
            int cantidadItems = venta.getDetalles() != null ? venta.getDetalles().size() : 0;
            PdfPCell itemsCell = new PdfPCell(new Phrase(String.valueOf(cantidadItems), textoNormal));
            itemsCell.setBackgroundColor(rowColor);
            itemsCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsCell.setPadding(8);
            itemsCell.setBorderColor(COLOR_MEDIUM_GRAY);
            itemsCell.setBorderWidth(0.5f);
            ventasTable.addCell(itemsCell);
            
            isAlternate = !isAlternate;
        }
        
        documento.add(ventasTable);
    }

    private static void addNoDataMessage(Document documento, Font textoNormal) throws DocumentException {
        // Professional no-data message
        PdfPTable messageTable = new PdfPTable(1);
        messageTable.setWidthPercentage(100);
        messageTable.setSpacingBefore(30);
          PdfPCell messageCell = new PdfPCell();
        messageCell.setBackgroundColor(COLOR_LIGHT_GRAY);
        messageCell.setPadding(20);
        messageCell.setBorderColor(COLOR_MEDIUM_GRAY);
        messageCell.setBorderWidth(1);
        
        Paragraph messageP = new Paragraph("No se encontraron ventas para el usuario en el período especificado.", textoNormal);
        messageP.setAlignment(Element.ALIGN_CENTER);
        messageCell.addElement(messageP);
        
        messageTable.addCell(messageCell);
        documento.add(messageTable);
    }
}
