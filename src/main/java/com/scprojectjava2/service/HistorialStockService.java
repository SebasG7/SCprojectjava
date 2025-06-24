package com.scprojectjava2.service;

import com.scprojectjava2.model.HistorialStock;
import com.scprojectjava2.model.Producto;
import com.scprojectjava2.repository.HistorialStockRepository;
import com.scprojectjava2.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@Service
public class HistorialStockService {

    @Autowired
    private HistorialStockRepository historialStockRepository;
    
    @Autowired
    private ProductoRepository productoRepository;

    // Registrar un movimiento de stock
    public void registrarMovimiento(Integer productoId, Integer stockAnterior, Integer stockNuevo, 
                                   String tipoMovimiento, String motivo, String usuario) {
        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto != null) {
            Integer cantidad = Math.abs(stockNuevo - stockAnterior);
            HistorialStock historial = new HistorialStock(producto, stockAnterior, stockNuevo, 
                                                         tipoMovimiento, cantidad, motivo, usuario);
            historialStockRepository.save(historial);
        }
    }

    // Obtener historial de un producto
    public List<HistorialStock> obtenerHistorialPorProducto(Integer productoId) {
        return historialStockRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
    }

    // Obtener historial entre fechas
    public List<HistorialStock> obtenerHistorialEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(LocalTime.MAX);
        return historialStockRepository.findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                inicioDateTime, finDateTime);
    }    // Obtener el stock de un producto en una fecha específica
    public Integer obtenerStockEnFecha(Integer productoId, LocalDate fecha) {
        LocalDateTime fechaDateTime = fecha.atTime(LocalTime.MAX);
        List<HistorialStock> movimientos = historialStockRepository.findStockAtDate(productoId, fechaDateTime);
        
        if (!movimientos.isEmpty()) {
            return movimientos.get(0).getStockNuevo();
        }
        
        // Si no hay historial, retornar el stock actual
        Producto producto = productoRepository.findById(productoId).orElse(null);
        return producto != null ? producto.getStock() : 0;
    }    // Obtener el stock de un producto al inicio de una fecha específica
    public Integer obtenerStockInicialEnFecha(Integer productoId, LocalDate fecha) {
        // Para obtener el stock al inicio del día, buscamos el último movimiento ANTES del inicio de ese día
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        List<HistorialStock> movimientos = historialStockRepository.findStockAtDate(productoId, inicioDelDia.minusSeconds(1));
        
        if (!movimientos.isEmpty()) {
            return movimientos.get(0).getStockNuevo();
        }
        
        // Si no hay historial anterior, revisar si hay movimientos posteriores para calcular el stock inicial
        // Si no hay movimientos posteriores, usar el stock actual
        List<HistorialStock> todosMovimientos = historialStockRepository.findByProductoIdOrderByFechaMovimientoDesc(productoId);
        
        if (todosMovimientos.isEmpty()) {
            // No hay historial, retornar el stock actual del producto
            Producto producto = productoRepository.findById(productoId).orElse(null);
            return producto != null ? producto.getStock() : 0;
        } else {
            // Hay movimientos posteriores, calcular el stock inicial trabajando hacia atrás
            // desde el primer movimiento registrado
            HistorialStock primerMovimiento = todosMovimientos.get(todosMovimientos.size() - 1);
            return primerMovimiento.getStockAnterior();
        }
    }

    // Obtener productos con movimientos entre fechas
    public List<Producto> obtenerProductosConMovimientosEntreFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
        LocalDateTime finDateTime = fechaFin.atTime(LocalTime.MAX);
        return historialStockRepository.findProductosWithMovementsBetweenDates(inicioDateTime, finDateTime);
    }

    // Obtener resumen de stock para reporte por fechas
    public Map<Integer, StockEnFecha> obtenerStockPorFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        Map<Integer, StockEnFecha> stockPorProducto = new HashMap<>();
        
        // Obtener todos los productos activos
        List<Producto> productos = productoRepository.findByActivoTrue();
          for (Producto producto : productos) {
            StockEnFecha stockInfo = new StockEnFecha();
            stockInfo.setProducto(producto);
            stockInfo.setStockInicial(obtenerStockInicialEnFecha(producto.getId(), fechaInicio));
            stockInfo.setStockFinal(obtenerStockEnFecha(producto.getId(), fechaFin));
            
            // Calcular movimientos en el período
            LocalDateTime inicioDateTime = fechaInicio.atStartOfDay();
            LocalDateTime finDateTime = fechaFin.atTime(LocalTime.MAX);
            List<HistorialStock> movimientos = historialStockRepository.findByProductoIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
                    producto.getId(), inicioDateTime, finDateTime);
            
            int entradas = 0;
            int salidas = 0;
            
            for (HistorialStock mov : movimientos) {
                if ("ENTRADA".equals(mov.getTipoMovimiento()) || "AJUSTE".equals(mov.getTipoMovimiento())) {
                    if (mov.getStockNuevo() > mov.getStockAnterior()) {
                        entradas += mov.getCantidad();
                    } else {
                        salidas += mov.getCantidad();
                    }
                } else if ("SALIDA".equals(mov.getTipoMovimiento()) || "VENTA".equals(mov.getTipoMovimiento())) {
                    salidas += mov.getCantidad();
                }
            }
            
            stockInfo.setEntradas(entradas);
            stockInfo.setSalidas(salidas);
            
            stockPorProducto.put(producto.getId(), stockInfo);
        }
        
        return stockPorProducto;
    }

    // Clase auxiliar para el reporte
    public static class StockEnFecha {
        private Producto producto;
        private Integer stockInicial;
        private Integer stockFinal;
        private Integer entradas;
        private Integer salidas;

        // Getters y Setters
        public Producto getProducto() { return producto; }
        public void setProducto(Producto producto) { this.producto = producto; }
        
        public Integer getStockInicial() { return stockInicial; }
        public void setStockInicial(Integer stockInicial) { this.stockInicial = stockInicial; }
        
        public Integer getStockFinal() { return stockFinal; }
        public void setStockFinal(Integer stockFinal) { this.stockFinal = stockFinal; }
        
        public Integer getEntradas() { return entradas; }
        public void setEntradas(Integer entradas) { this.entradas = entradas; }
        
        public Integer getSalidas() { return salidas; }
        public void setSalidas(Integer salidas) { this.salidas = salidas; }
        
        public Integer getVariacion() { 
            return stockFinal - stockInicial; 
        }
    }
}
