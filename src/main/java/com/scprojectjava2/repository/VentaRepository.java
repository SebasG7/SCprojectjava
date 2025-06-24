package com.scprojectjava2.repository;

import com.scprojectjava2.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    
    // Obtener todas las ventas ordenadas por fecha descendente
    List<Venta> findAllByOrderByFechaDesc();
    
    // Obtener ventas por correo del cliente
    List<Venta> findByCorreoClienteOrderByFechaDesc(String correoCliente);
    
    // Obtener ventas en un rango de fechas
    List<Venta> findByFechaBetweenOrderByFechaDesc(LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Obtener ventas con total mayor a un valor específico
    List<Venta> findByTotalGreaterThanOrderByFechaDesc(Double total);
    
    // Query personalizada para obtener ventas con sus detalles completos
    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "ORDER BY v.fecha DESC")
    List<Venta> findAllVentasWithDetalles();
    
    // Query para obtener ventas por cliente con sus detalles
    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE v.correoCliente = :correoCliente " +
           "ORDER BY v.fecha DESC")
    List<Venta> findVentasByClienteWithDetalles(@Param("correoCliente") String correoCliente);
    
    // Obtener total de ventas por mes/año
    @Query("SELECT SUM(v.total) FROM Venta v " +
           "WHERE YEAR(v.fecha) = :year AND MONTH(v.fecha) = :month")
    Double getTotalVentasByMes(@Param("year") int year, @Param("month") int month);
    
    // Contar ventas por cliente
    Long countByCorreoCliente(String correoCliente);
    
    // Obtener las últimas N ventas
    List<Venta> findTop10ByOrderByFechaDesc();
    
    // Query para obtener una venta por ID con sus detalles y productos (fetch join)
    @Query("SELECT v FROM Venta v LEFT JOIN FETCH v.detalles d LEFT JOIN FETCH d.producto WHERE v.id = :id")
    Venta findByIdWithDetalles(@Param("id") Long id);
    
    // Obtener ventas por usuario cajero
    List<Venta> findByUsuarioCajeroOrderByFechaDesc(String usuarioCajero);
    
    // Obtener las últimas N ventas por usuario cajero
    List<Venta> findTop10ByUsuarioCajeroOrderByFechaDesc(String usuarioCajero);
    
    // Obtener ventas por usuario cajero con detalles
    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE v.usuarioCajero = :usuarioCajero " +
           "ORDER BY v.fecha DESC")
    List<Venta> findVentasByUsuarioCajeroWithDetalles(@Param("usuarioCajero") String usuarioCajero);
    
    // Obtener ventas por usuario cajero en un rango de fechas
    List<Venta> findByUsuarioCajeroAndFechaBetweenOrderByFechaDesc(
        String usuarioCajero, LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Contar ventas por usuario cajero
    Long countByUsuarioCajero(String usuarioCajero);
    
    // Obtener total de ingresos por usuario cajero
    @Query("SELECT COALESCE(SUM(v.total), 0) FROM Venta v WHERE v.usuarioCajero = :usuarioCajero")
    Double getTotalIngresosByUsuarioCajero(@Param("usuarioCajero") String usuarioCajero);
    
    // Obtener ventas por usuario cajero en un rango de fechas con detalles
    @Query("SELECT DISTINCT v FROM Venta v " +
           "LEFT JOIN FETCH v.detalles d " +
           "LEFT JOIN FETCH d.producto " +
           "WHERE v.usuarioCajero = :usuarioCajero " +
           "AND v.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY v.fecha DESC")
    List<Venta> findVentasByUsuarioCajeroAndFechaBetweenWithDetalles(
        @Param("usuarioCajero") String usuarioCajero,
        @Param("fechaInicio") LocalDateTime fechaInicio,
        @Param("fechaFin") LocalDateTime fechaFin);
}