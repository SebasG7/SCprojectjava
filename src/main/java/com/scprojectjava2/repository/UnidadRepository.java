package com.scprojectjava2.repository;

import com.scprojectjava2.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Integer> {
    
    @Query("SELECT u FROM Unidad u WHERE u.activo = 1")
    List<Unidad> findAllActivos();
    
    @Query("SELECT u FROM Unidad u WHERE u.activo = 0")
    List<Unidad> findAllInactivos();
}
