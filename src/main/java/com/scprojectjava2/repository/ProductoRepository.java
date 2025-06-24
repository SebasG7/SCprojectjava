package com.scprojectjava2.repository;

import com.scprojectjava2.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    // Métodos existentes (activo = 1 means active, activo = 0 means inactive)
    List<Producto> findByActivoOrderByNombre(int activo);
    List<Producto> findByActivo(int activo);
    List<Producto> findByCategoriaIdAndActivoOrderByNombre(Integer categoriaId, int activo);
    
    // Búsqueda por código único
    Producto findByCodigo(String codigo);
    Producto findByCodigoAndActivo(String codigo, int activo);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Integer id);
      // Nuevos métodos para búsqueda
    
    // Buscar por nombre (case insensitive) y activos
    List<Producto> findByNombreContainingIgnoreCaseAndActivoOrderByNombre(String nombre, int activo);
    
    // Buscar por nombre y categoría específica
    List<Producto> findByNombreContainingIgnoreCaseAndCategoriaIdAndActivoOrderByNombre(String nombre, Integer categoriaId, int activo);
    
    // Buscar por descripción (case insensitive) y activos
    List<Producto> findByDescripcionContainingIgnoreCaseAndActivoOrderByNombre(String descripcion, int activo);
    
    // Búsqueda combinada por nombre O descripción
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +           "ORDER BY p.nombre")
    List<Producto> findByNombreOrDescripcionContainingIgnoreCaseAndActivo(@Param("texto") String texto, @Param("activo") int activo);
    
    // Búsqueda combinada por nombre O descripción Y categoría
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND p.categoria.id = :categoriaId AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "ORDER BY p.nombre")
    List<Producto> findByNombreOrDescripcionContainingIgnoreCaseAndCategoriaIdAndActivo(
            @Param("texto") String texto, @Param("categoriaId") Integer categoriaId, @Param("activo") int activo);
    
    // Buscar productos with stock mayor a cero
    List<Producto> findByActivoAndStockGreaterThanOrderByNombre(int activo, Integer stock);
      // Buscar por rango de precios
    List<Producto> findByActivoAndPrecioBetweenOrderByPrecio(int activo, Double precioMin, Double precioMax);
    
    // Buscar productos de una categoría con stock disponible
    List<Producto> findByCategoriaIdAndActivoAndStockGreaterThanOrderByNombre(Integer categoriaId, int activo, Integer stock);
    
    // Contar productos por categoría
    Long countByCategoriaIdAndActivo(Integer categoriaId, int activo);
    
    // Obtener productos más caros de una categoría
    List<Producto> findTop5ByCategoriaIdAndActivoOrderByPrecioDesc(Integer categoriaId, int activo);
    
    // Obtener productos más baratos
    List<Producto> findTop10ByActivoOrderByPrecioAsc(int activo);
    
    // Convenience methods for active products (activo = 1)
    default List<Producto> findByActivoTrueOrderByNombre() {
        return findByActivoOrderByNombre(1);
    }
    
    default List<Producto> findByActivoTrue() {
        return findByActivo(1);
    }
    
    default List<Producto> findByActivoFalse() {
        return findByActivo(0);
    }
    
    default List<Producto> findByCategoriaIdAndActivoTrueOrderByNombre(Integer categoriaId) {
        return findByCategoriaIdAndActivoOrderByNombre(categoriaId, 1);
    }
    
    default List<Producto> findByNombreContainingIgnoreCaseAndActivoTrueOrderByNombre(String nombre) {
        return findByNombreContainingIgnoreCaseAndActivoOrderByNombre(nombre, 1);
    }
    
    default List<Producto> findByNombreOrDescripcionContainingIgnoreCaseAndActivoTrue(String texto) {
        return findByNombreOrDescripcionContainingIgnoreCaseAndActivo(texto, 1);
    }
    
    // Búsqueda combinada por código O nombre O descripción
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "(LOWER(p.codigo) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))) " +
           "ORDER BY p.nombre")
    List<Producto> findByCodigoOrNombreOrDescripcionContainingIgnoreCaseAndActivo(@Param("texto") String texto, @Param("activo") int activo);    // Método para encontrar productos que vencen en los próximos días (especificados)
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "p.fechaVencimiento IS NOT NULL AND " +
           "p.fechaVencimiento > CURRENT_DATE AND " +
           "p.fechaVencimiento <= :fechaLimite " +
           "ORDER BY p.fechaVencimiento ASC")
    List<Producto> findProductosProximosAVencer(@Param("activo") int activo, @Param("fechaLimite") java.time.LocalDate fechaLimite);    // Convenience method for active products expiring within 5 days
    default List<Producto> findProductosProximosAVencerActivos() {
        java.time.LocalDate fechaLimite = java.time.LocalDate.now().plusDays(5);
        return findProductosProximosAVencer(1, fechaLimite);
    }
    
    // Método para encontrar productos con stock bajo (menos de 10 unidades)
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "p.stock < :stockMinimo " +
           "ORDER BY p.stock ASC, p.nombre ASC")
    List<Producto> findProductosConStockBajo(@Param("activo") int activo, @Param("stockMinimo") Integer stockMinimo);
      // Convenience method for active products with low stock (less than 10 units)
    default List<Producto> findProductosConStockBajoActivos() {
        return findProductosConStockBajo(1, 10);
    }
      // Método para encontrar productos vencidos (fecha de vencimiento menor o igual a hoy)
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "p.fechaVencimiento IS NOT NULL AND " +
           "p.fechaVencimiento <= CURRENT_DATE " +
           "ORDER BY p.fechaVencimiento ASC")
    List<Producto> findProductosVencidos(@Param("activo") int activo);
    
    // Convenience method for active expired products
    default List<Producto> findProductosVencidosActivos() {
        return findProductosVencidos(1);
    }

    // Método para encontrar productos agotados (stock = 0)
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "p.stock = 0 " +
           "ORDER BY p.nombre ASC")
    List<Producto> findProductosAgotados(@Param("activo") int activo);
    
    // Convenience method for active out-of-stock products
    default List<Producto> findProductosAgotadosActivos() {
        return findProductosAgotados(1);
    }

    // Método para encontrar productos no vendibles (vencidos o agotados)
    @Query("SELECT p FROM Producto p WHERE p.activo = :activo AND " +
           "(p.stock = 0 OR (p.fechaVencimiento IS NOT NULL AND p.fechaVencimiento <= CURRENT_DATE)) " +
           "ORDER BY p.nombre ASC")
    List<Producto> findProductosNoVendibles(@Param("activo") int activo);
      // Convenience method for active non-sellable products
    default List<Producto> findProductosNoVendiblesActivos() {
        return findProductosNoVendibles(1);
    }    // Métodos para verificar si una categoría o unidad están en uso por productos activos
    boolean existsByCategoriaIdAndActivo(Integer categoriaId, int activo);
    boolean existsByUnidadIdAndActivo(Integer unidadId, int activo);
}
