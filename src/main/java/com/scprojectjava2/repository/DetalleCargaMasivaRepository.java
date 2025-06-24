package com.scprojectjava2.repository;

import com.scprojectjava2.model.DetalleCargaMasiva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCargaMasivaRepository extends JpaRepository<DetalleCargaMasiva, Integer> {
    
    // Obtener detalles por operación
    List<DetalleCargaMasiva> findByOperacionCargaIdOrderByLineaArchivo(Integer operacionId);
    
    // Obtener detalles fallidos por operación
    List<DetalleCargaMasiva> findByOperacionCargaIdAndEstadoOrderByLineaArchivo(
            Integer operacionId, String estado);
    
    // Obtener detalles exitosos por operación
    List<DetalleCargaMasiva> findByOperacionCargaIdAndEstadoAndAccionRealizadaOrderByLineaArchivo(
            Integer operacionId, String estado, String accion);
    
    // Contar detalles por estado en una operación
    @Query("SELECT COUNT(d) FROM DetalleCargaMasiva d WHERE d.operacionCarga.id = :operacionId AND d.estado = :estado")
    Long countByOperacionIdAndEstado(@Param("operacionId") Integer operacionId, @Param("estado") String estado);
    
    // Obtener productos creados en una operación específica
    @Query("SELECT d FROM DetalleCargaMasiva d WHERE d.operacionCarga.id = :operacionId " +
           "AND d.estado = 'EXITOSO' AND d.accionRealizada = 'CREADO' " +
           "ORDER BY d.lineaArchivo")
    List<DetalleCargaMasiva> findProductosCreadosEnOperacion(@Param("operacionId") Integer operacionId);
    
    // Obtener productos actualizados en una operación específica
    @Query("SELECT d FROM DetalleCargaMasiva d WHERE d.operacionCarga.id = :operacionId " +
           "AND d.estado = 'EXITOSO' AND d.accionRealizada = 'ACTUALIZADO' " +
           "ORDER BY d.lineaArchivo")
    List<DetalleCargaMasiva> findProductosActualizadosEnOperacion(@Param("operacionId") Integer operacionId);
}
