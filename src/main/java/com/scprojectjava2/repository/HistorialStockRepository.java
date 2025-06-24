package com.scprojectjava2.repository;

import com.scprojectjava2.model.HistorialStock;
import com.scprojectjava2.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistorialStockRepository extends JpaRepository<HistorialStock, Integer> {
    
    // Obtener historial por producto
    List<HistorialStock> findByProductoIdOrderByFechaMovimientoDesc(Integer productoId);
    
    // Obtener historial por producto entre fechas
    List<HistorialStock> findByProductoIdAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            Integer productoId, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Obtener historial entre fechas para todos los productos
    List<HistorialStock> findByFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Obtener el último movimiento de stock para un producto antes de una fecha específica
    @Query("SELECT h FROM HistorialStock h WHERE h.producto.id = :productoId " +
           "AND h.fechaMovimiento <= :fecha " +
           "ORDER BY h.fechaMovimiento DESC")
    List<HistorialStock> findLastStockMovementBeforeDate(@Param("productoId") Integer productoId, 
                                                         @Param("fecha") LocalDateTime fecha);
    
    // Obtener el stock de un producto en una fecha específica
    @Query("SELECT h FROM HistorialStock h WHERE h.producto.id = :productoId " +
           "AND h.fechaMovimiento <= :fecha " +
           "ORDER BY h.fechaMovimiento DESC")
    List<HistorialStock> findStockAtDate(@Param("productoId") Integer productoId, 
                                        @Param("fecha") LocalDateTime fecha);
    
    // Obtener todos los productos que tuvieron movimientos entre fechas
    @Query("SELECT DISTINCT h.producto FROM HistorialStock h " +
           "WHERE h.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY h.producto.nombre")
    List<Producto> findProductosWithMovementsBetweenDates(@Param("fechaInicio") LocalDateTime fechaInicio,
                                                         @Param("fechaFin") LocalDateTime fechaFin);
    
    // Obtener historial por tipo de movimiento
    List<HistorialStock> findByTipoMovimientoAndFechaMovimientoBetweenOrderByFechaMovimientoDesc(
            String tipoMovimiento, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Obtener resumen de movimientos por producto entre fechas
    @Query("SELECT h.producto, SUM(CASE WHEN h.tipoMovimiento = 'ENTRADA' THEN h.cantidad ELSE 0 END) as entradas, " +
           "SUM(CASE WHEN h.tipoMovimiento = 'SALIDA' THEN h.cantidad ELSE 0 END) as salidas " +
           "FROM HistorialStock h " +
           "WHERE h.fechaMovimiento BETWEEN :fechaInicio AND :fechaFin " +
           "GROUP BY h.producto " +
           "ORDER BY h.producto.nombre")
    List<Object[]> getMovementSummaryByProduct(@Param("fechaInicio") LocalDateTime fechaInicio,
                                              @Param("fechaFin") LocalDateTime fechaFin);
}
