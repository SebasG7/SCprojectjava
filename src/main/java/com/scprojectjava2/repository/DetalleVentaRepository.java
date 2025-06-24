package com.scprojectjava2.repository;
import com.scprojectjava2.model.DetalleVenta;
import com.scprojectjava2.model.Producto;
import com.scprojectjava2.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {
    
    // Obtener todos los detalles de una venta específica
    List<DetalleVenta> findByVentaOrderById(Venta venta);
    
    // Obtener detalles por ID de venta
    List<DetalleVenta> findByVentaIdOrderById(Long ventaId);
    
    // Obtener detalles por producto
    List<DetalleVenta> findByProductoOrderByVentaFechaDesc(Producto producto);
    
    // Obtener detalles por ID de producto
    List<DetalleVenta> findByProductoIdOrderByVentaFechaDesc(Long productoId);
    
    // Calcular total vendido de un producto específico
    @Query("SELECT COALESCE(SUM(d.cantidad), 0) FROM DetalleVenta d WHERE d.producto.id = :productoId")
    Integer getTotalCantidadVendidaByProducto(@Param("productoId") Long productoId);
    
    // Obtener los productos más vendidos
    @Query("SELECT d.producto.id, d.producto.nombre, SUM(d.cantidad) as totalVendido " +
           "FROM DetalleVenta d " +
           "GROUP BY d.producto.id, d.producto.nombre " +
           "ORDER BY totalVendido DESC")
    List<Object[]> getProductosMasVendidos();
    
    // Obtener ingresos por producto
    @Query("SELECT d.producto.id, d.producto.nombre, SUM(d.subtotal) as totalIngresos " +
           "FROM DetalleVenta d " +
           "GROUP BY d.producto.id, d.producto.nombre " +
           "ORDER BY totalIngresos DESC")
    List<Object[]> getIngresosByProducto();
    
    // Eliminar detalles por venta (útil para operaciones de limpieza)
    void deleteByVenta(Venta venta);
    
    void deleteByVentaId(Long ventaId);
    
    // Contar detalles de una venta
    Long countByVentaId(Long ventaId);
    
    // Obtener el subtotal de una venta
    @Query("SELECT COALESCE(SUM(d.subtotal), 0) FROM DetalleVenta d WHERE d.venta.id = :ventaId")
    Double getSubtotalByVentaId(@Param("ventaId") Long ventaId);
}