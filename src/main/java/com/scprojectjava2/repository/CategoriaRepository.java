package com.scprojectjava2.repository;

import com.scprojectjava2.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
      // Métodos existentes (activo = 1 means active, activo = 0 means inactive)
    List<Categoria> findByActivoOrderByNombre(Integer activo);
    List<Categoria> findByActivo(Integer activo);
    
    // Buscar categorías por nombre
    List<Categoria> findByNombreContainingIgnoreCaseAndActivoOrderByNombre(String nombre, Integer activo);
    
    // Buscar categorías por descripción
    List<Categoria> findByDescripcionContainingIgnoreCaseAndActivoOrderByNombre(String descripcion, Integer activo);
    
    // Contar categorías activas
    Long countByActivo(Integer activo);
    
    // Convenience methods for active categories (activo = 1)
    default List<Categoria> findByActivoTrueOrderByNombre() {
        return findByActivoOrderByNombre(1);
    }
    
    default List<Categoria> findByActivoTrue() {
        return findByActivo(1);
    }
    
    default List<Categoria> findByActivoFalse() {
        return findByActivo(0);
    }
    
    default List<Categoria> findByNombreContainingIgnoreCaseAndActivoTrueOrderByNombre(String nombre) {
        return findByNombreContainingIgnoreCaseAndActivoOrderByNombre(nombre, 1);
    }
    
    default Long countByActivoTrue() {
        return countByActivo(1);
    }
}
