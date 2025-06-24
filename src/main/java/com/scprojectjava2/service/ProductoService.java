package com.scprojectjava2.service;

import com.scprojectjava2.model.Producto;
import com.scprojectjava2.repository.ProductoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    
    @Autowired
    private HistorialStockService historialStockService;

    public List<Producto> listarActivos() {
        return productoRepository.findByActivoTrue();
    }

    public List<Producto> listarInactivos() {
        return productoRepository.findByActivoFalse();
    }

    public Producto obtenerPorId(Integer id) {
        return productoRepository.findById(id).orElse(null);
    }

    public Producto obtenerPorCodigo(String codigo) {
        return productoRepository.findByCodigo(codigo);
    }

    public boolean existeCodigo(String codigo) {
        return productoRepository.existsByCodigo(codigo);
    }

    public boolean existeCodigoParaOtroProducto(String codigo, Integer id) {
        return productoRepository.existsByCodigoAndIdNot(codigo, id);
    }

    public void agregar(Producto producto) {
        producto.setActivo(1);
        productoRepository.save(producto);
        
        if (producto.getStock() > 0) {
            historialStockService.registrarMovimiento(
                producto.getId(), 0, producto.getStock(), 
                "ENTRADA", "Stock inicial del producto", "Sistema");
        }
    }

    public void actualizar(Producto producto) {
        Optional<Producto> productoExistente = productoRepository.findById(producto.getId());
        if (productoExistente.isPresent()) {
            Producto anterior = productoExistente.get();
            
            // Registrar movimiento de stock si cambió
            if (anterior.getStock() != producto.getStock()) {
                String tipoMovimiento = producto.getStock() > anterior.getStock() ? "ENTRADA" : "SALIDA";
                String motivo = "Actualización de producto";
                
                historialStockService.registrarMovimiento(
                    producto.getId(), anterior.getStock(), producto.getStock(), 
                    tipoMovimiento, motivo, "Sistema");
            }
            
            // Registrar reactivación si se cambió de inactivo a activo
            if (anterior.getActivo() == 0 && producto.getActivo() == 1) {
                historialStockService.registrarMovimiento(
                    producto.getId(), anterior.getStock(), producto.getStock(),
                    "REACTIVACION", "Producto reactivado", "Sistema");
            }
        }
        productoRepository.save(producto);
    }
    
    public void actualizarStock(Integer productoId, Integer nuevoStock, String motivo, String usuario) {
        Optional<Producto> productoOpt = productoRepository.findById(productoId);
        if (productoOpt.isPresent()) {
            Producto producto = productoOpt.get();
            Integer stockAnterior = producto.getStock();
            
            producto.setStock(nuevoStock);
            productoRepository.save(producto);
            
            // Determinar el tipo de movimiento
            String tipoMovimiento;
            if (nuevoStock > stockAnterior) {
                tipoMovimiento = "ENTRADA";
            } else if (nuevoStock < stockAnterior) {
                tipoMovimiento = "SALIDA";
            } else {
                tipoMovimiento = "AJUSTE";
            }
            
            // Registrar el movimiento en el historial
            historialStockService.registrarMovimiento(
                productoId, stockAnterior, nuevoStock, 
                tipoMovimiento, motivo, usuario);
        }
    }

    public void eliminar(Integer id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        productoOpt.ifPresent(producto -> {
            producto.setActivo(0);
            productoRepository.save(producto);
        });
    }

    public void reactivar(Integer id) {
        Optional<Producto> productoOpt = productoRepository.findById(id);
        productoOpt.ifPresent(producto -> {
            producto.setActivo(1);
            productoRepository.save(producto);
        });
    }
    
    public List<Producto> obtenerProductosProximosAVencer() {
        return productoRepository.findProductosProximosAVencerActivos();
    }
    
    public List<Producto> obtenerProductosConStockBajo() {
        return productoRepository.findProductosConStockBajoActivos();
    }
    
    public List<Producto> obtenerProductosVencidos() {
        return productoRepository.findProductosVencidosActivos();
    }

    public List<Producto> obtenerProductosAgotados() {
        return productoRepository.findProductosAgotadosActivos();
    }

    public List<Producto> obtenerProductosNoVendibles() {
        return productoRepository.findProductosNoVendiblesActivos();
    }

    // Métodos para verificar si una categoría o unidad están en uso
    public boolean existeProductoConCategoria(Integer categoriaId) {
        return productoRepository.existsByCategoriaIdAndActivo(categoriaId, 1);
    }

    public boolean existeProductoConUnidad(Integer unidadId) {
        return productoRepository.existsByUnidadIdAndActivo(unidadId, 1);
    }
}
