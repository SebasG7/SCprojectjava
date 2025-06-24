package com.scprojectjava2.repository;

import com.scprojectjava2.model.OperacionCargaMasiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperacionCargaMasivaRepository extends JpaRepository<OperacionCargaMasiva, Integer> {
    
    // Obtener operaciones por usuario
    List<OperacionCargaMasiva> findByUsuarioOrderByFechaOperacionDesc(String usuario);
    
    // Obtener operaciones entre fechas
    List<OperacionCargaMasiva> findByFechaOperacionBetweenOrderByFechaOperacionDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);
    
    // Obtener operaciones por estado
    List<OperacionCargaMasiva> findByEstadoOrderByFechaOperacionDesc(String estado);
    
    // Obtener todas las operaciones ordenadas por fecha
    List<OperacionCargaMasiva> findAllByOrderByFechaOperacionDesc();
    
    // Obtener estadísticas de operaciones
    @Query("SELECT COUNT(o) FROM OperacionCargaMasiva o WHERE o.estado = :estado")
    Long countByEstado(@Param("estado") String estado);
    
    // Obtener total de productos procesados en un rango de fechas
    @Query("SELECT SUM(o.totalProductos) FROM OperacionCargaMasiva o " +
           "WHERE o.fechaOperacion BETWEEN :fechaInicio AND :fechaFin")
    Long getTotalProductosProcesados(@Param("fechaInicio") LocalDateTime fechaInicio,
                                    @Param("fechaFin") LocalDateTime fechaFin);
    
    // Obtener operaciones revertidas
    List<OperacionCargaMasiva> findByRevertidaTrueOrderByFechaOperacionDesc();
    
    // Obtener operaciones no revertidas
    List<OperacionCargaMasiva> findByRevertidaFalseOrderByFechaOperacionDesc();
    
    // Obtener operaciones con productos fallidos
    @Query("SELECT o FROM OperacionCargaMasiva o WHERE o.productosFallidos > 0 " +
           "ORDER BY o.fechaOperacion DESC")
    List<OperacionCargaMasiva> findOperacionesConFallidos();
}
